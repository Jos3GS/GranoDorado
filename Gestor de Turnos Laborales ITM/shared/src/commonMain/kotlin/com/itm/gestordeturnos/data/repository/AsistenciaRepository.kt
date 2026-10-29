package com.itm.gestordeturnos.data.repository

import com.itm.gestordeturnos.data.mock.MockDataProviders
import com.itm.gestordeturnos.data.service.FirestoreService
import com.itm.gestordeturnos.domain.model.EstadoAsistencia
import com.itm.gestordeturnos.domain.model.NotificacionIncumplimiento
import com.itm.gestordeturnos.domain.model.RegistroAsistencia
import com.itm.gestordeturnos.domain.repository.AsistenciaRepositoryContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object AsistenciaRepository : AsistenciaRepositoryContract {
    private val repositoryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val _notificaciones = MutableStateFlow(MockDataProviders.inicialIncumplimientos())
    override val notificaciones: StateFlow<List<NotificacionIncumplimiento>> = _notificaciones.asStateFlow()

    private val _registrosDiarios = MutableStateFlow(MockDataProviders.inicialRegistrosAsistencia())
    override val registrosDiarios: StateFlow<List<RegistroAsistencia>> = _registrosDiarios.asStateFlow()

    private var jobSincronizacion: Job? = null

    init {
        iniciarSincronizacionFirestore()
    }

    override fun iniciarSincronizacionFirestore(forzar: Boolean) {
        if (!forzar && jobSincronizacion?.isActive == true) return
        jobSincronizacion?.cancel()
        jobSincronizacion = repositoryScope.launch {
            try {
                // 1. Cargar registros de asistencia
                val remotos = FirestoreService.obtenerAsistencia()
                if (remotos.isNotEmpty()) {
                    _registrosDiarios.value = remotos
                }
                launch {
                    FirestoreService.observarAsistencia().collect { listaRemota ->
                        if (listaRemota.isNotEmpty()) {
                            _registrosDiarios.value = listaRemota
                        }
                    }
                }

                // 2. Cargar notificaciones de incumplimientos
                val remotosInc = FirestoreService.obtenerIncumplimientos()
                if (remotosInc.isNotEmpty()) {
                    _notificaciones.value = remotosInc
                } else {
                    FirestoreService.inicializarIncumplimientosSiEstaVacio()
                    val cargados = FirestoreService.obtenerIncumplimientos()
                    if (cargados.isNotEmpty()) {
                        _notificaciones.value = cargados
                    }
                }
                launch {
                    FirestoreService.observarIncumplimientos().collect { listaRemota ->
                        if (listaRemota.isNotEmpty()) {
                            _notificaciones.value = listaRemota
                        }
                    }
                }
            } catch (e: Exception) {
                println("Error en sincronización de Asistencia: ${e.message}")
            }
        }
    }

    override fun detenerSincronizacion() {
        jobSincronizacion?.cancel()
        jobSincronizacion = null
    }

    override suspend fun guardarRegistroAsistencia(registro: RegistroAsistencia): Result<Unit> {
        // Optimistic update local
        _registrosDiarios.update { existentes ->
            val filtrados = existentes.filterNot {
                it.id == registro.id || (it.empleadoId == registro.empleadoId && it.fechaIso == registro.fechaIso)
            }
            filtrados + registro
        }
        val res = FirestoreService.guardarAsistencia(registro)
        if (res.isFailure) {
            println("Error al guardar registro de asistencia en Firestore: ${res.exceptionOrNull()?.message}")
        }
        return res
    }

    override fun guardarRegistroAsistenciaEnBackground(registro: RegistroAsistencia) {
        repositoryScope.launch {
            guardarRegistroAsistencia(registro)
        }
    }

    override fun registrarIncumplimiento(notif: NotificacionIncumplimiento) {
        repositoryScope.launch {
            guardarIncumplimiento(notif)
        }
    }

    override suspend fun guardarIncumplimiento(notif: NotificacionIncumplimiento): Result<Unit> {
        _notificaciones.update { existentes ->
            val sinDuplicado = existentes.filterNot { it.id == notif.id }
            listOf(notif) + sinDuplicado
        }
        val res = FirestoreService.guardarIncumplimiento(notif)
        if (res.isFailure) {
            println("Error al guardar incumplimiento en Firestore: ${res.exceptionOrNull()?.message}")
        }
        return res
    }

    override suspend fun atenderIncumplimiento(id: String, motivo: String, horaAtencion: String): Result<Unit> {
        val actual = _notificaciones.value.find { it.id == id } ?: return Result.failure(Exception("Notificación no encontrada"))
        val actualizada = actual.copy(
            atendida = true,
            motivoAtencion = motivo,
            horaAtencion = horaAtencion
        )
        return guardarIncumplimiento(actualizada)
    }

    override suspend fun resolverIncumplimientoPorLlegada(empleadoId: String, fechaIso: String): Result<Unit> {
        val pendientes = _notificaciones.value.filter {
            it.empleadoId == empleadoId && it.fechaIso == fechaIso && !it.atendida
        }
        for (notif in pendientes) {
            val resuelta = notif.copy(
                atendida = true,
                motivoAtencion = "Empleado marcó ingreso tardío",
                horaAtencion = ""
            )
            guardarIncumplimiento(resuelta)
        }
        return Result.success(Unit)
    }

    override suspend fun registrarMarcaSupervisor(
        registroId: String,
        empleadoId: String,
        empleadoNombre: String,
        sucursal: String,
        fechaIso: String,
        tipoTurno: String,
        horarioProg: String,
        tipoMarca: String,
        horaMarca: String,
        motivo: String
    ): Result<Unit> {
        if (motivo.isBlank()) {
            return Result.failure(IllegalArgumentException("El motivo de la marcación por supervisor es obligatorio."))
        }

        val existente = _registrosDiarios.value.find {
            it.id == registroId || (it.empleadoId == empleadoId && it.fechaIso == fechaIso)
        }

        var entrada = existente?.horaEntrada ?: "-"
        var salida = existente?.horaSalida ?: "-"
        var descIni = existente?.inicioDescanso ?: "-"
        var descFin = existente?.finDescanso ?: "-"
        var estadoAsis = existente?.estadoAsistencia ?: EstadoAsistencia.SIN_INICIAR
        var estadoStr = existente?.estado ?: "PENDIENTE"
        var minsTarde = existente?.minutosTarde ?: 0

        when (tipoMarca) {
            "ENTRADA" -> {
                if (existente != null && existente.horaEntrada != "-" && !existente.registradoPorSupervisor) {
                    return Result.failure(IllegalStateException("La marcación de entrada ya fue registrada por el empleado y no puede sobrescribirse."))
                }
                entrada = horaMarca
                estadoAsis = EstadoAsistencia.EN_TURNO
                val partes = horarioProg.split("-")
                if (partes.isNotEmpty()) {
                    val horaIniProgMin = parsearMinutos(partes[0]) ?: 0
                    val horaEntradaMin = parsearMinutos(horaMarca) ?: 0
                    minsTarde = maxOf(0, horaEntradaMin - horaIniProgMin)
                }
                estadoStr = if (minsTarde > 0) "TARDE" else "PRESENTE"
            }
            "INICIO_DESCANSO" -> {
                if (existente != null && existente.inicioDescanso != "-" && !existente.registradoPorSupervisor) {
                    return Result.failure(IllegalStateException("El inicio de descanso ya fue registrado por el empleado."))
                }
                descIni = horaMarca
                estadoAsis = EstadoAsistencia.EN_DESCANSO
                estadoStr = "EN DESCANSO"
            }
            "FIN_DESCANSO" -> {
                if (existente != null && existente.finDescanso != "-" && !existente.registradoPorSupervisor) {
                    return Result.failure(IllegalStateException("El fin de descanso ya fue registrado por el empleado."))
                }
                descFin = horaMarca
                estadoAsis = EstadoAsistencia.EN_TURNO
                estadoStr = if (minsTarde > 0) "TARDE" else "PRESENTE"
            }
            "SALIDA" -> {
                if (existente != null && existente.horaSalida != "-" && !existente.registradoPorSupervisor) {
                    return Result.failure(IllegalStateException("La marcación de salida ya fue registrada por el empleado."))
                }
                salida = horaMarca
                estadoAsis = EstadoAsistencia.FINALIZADO
                estadoStr = "FINALIZADO"
            }
        }

        val minsTrabajados = if (entrada != "-" && salida != "-") {
            calcularMinutosTrabajados(entrada, salida, descIni, descFin)
        } else {
            existente?.tiempoEfectivoMinutos ?: 0
        }

        val registroActualizado = RegistroAsistencia(
            id = if (registroId.isNotBlank()) registroId else "${empleadoId}_${fechaIso}",
            empleadoId = empleadoId,
            empleado = empleadoNombre.ifBlank { existente?.empleado ?: "Empleado" },
            sucursal = sucursal.ifBlank { existente?.sucursal ?: "CENTRO" },
            fechaIso = fechaIso,
            tipoTurno = tipoTurno.ifBlank { existente?.tipoTurno ?: "Turno General" },
            horarioProgramado = horarioProg.ifBlank { existente?.horarioProgramado ?: "" },
            horaEntrada = entrada,
            horaSalida = salida,
            inicioDescanso = descIni,
            finDescanso = descFin,
            estado = estadoStr,
            estadoAsistencia = estadoAsis,
            tiempoEfectivoMinutos = minsTrabajados,
            minutosTarde = minsTarde,
            esDescanso = existente?.esDescanso ?: false,
            registradoPorSupervisor = true,
            motivoSupervisor = motivo
        )

        val resultado = guardarRegistroAsistencia(registroActualizado)
        if (resultado.isSuccess && tipoMarca == "ENTRADA") {
            resolverIncumplimientoPorLlegada(empleadoId, fechaIso)
        }
        return resultado
    }

    private fun parsearMinutos(horaStr: String): Int? {
        val limpia = horaStr.trim()
        val partes = limpia.split(":")
        if (partes.size == 2) {
            val h = partes[0].toIntOrNull() ?: return null
            val m = partes[1].toIntOrNull() ?: return null
            return h * 60 + m
        }
        return null
    }

    private fun calcularMinutosTrabajados(
        entrada: String,
        salida: String,
        inicioDesc: String,
        finDesc: String
    ): Int {
        val ini = parsearMinutos(entrada) ?: return 0
        val fin = parsearMinutos(salida) ?: return 0
        var total = fin - ini
        if (total < 0) total += 24 * 60

        val dIni = parsearMinutos(inicioDesc)
        val dFin = parsearMinutos(finDesc)
        if (dIni != null && dFin != null) {
            var durDesc = dFin - dIni
            if (durDesc < 0) durDesc += 24 * 60
            total = maxOf(0, total - durDesc)
        }
        return total
    }

    override fun actualizarNotificaciones(lista: List<NotificacionIncumplimiento>) {
        _notificaciones.value = lista
    }
}
