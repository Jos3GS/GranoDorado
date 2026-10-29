package com.itm.gestordeturnos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itm.gestordeturnos.data.repository.EmpleadoRepository
import com.itm.gestordeturnos.data.repository.NovedadRepository
import com.itm.gestordeturnos.data.repository.TurnoRepository
import com.itm.gestordeturnos.domain.model.AsignacionTurno
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.EstadoNovedad
import com.itm.gestordeturnos.domain.model.EstadoUsuario
import com.itm.gestordeturnos.domain.model.Novedad
import com.itm.gestordeturnos.domain.model.RolUsuario
import com.itm.gestordeturnos.domain.repository.EmpleadoRepositoryContract
import com.itm.gestordeturnos.domain.repository.NovedadRepositoryContract
import com.itm.gestordeturnos.domain.repository.TurnoRepositoryContract
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class NovedadesState(
    val novedades: List<Novedad> = emptyList(),
    val empleados: List<Empleado> = emptyList(),
    val asignaciones: List<AsignacionTurno> = emptyList(),
    val filtroEstado: String = "Todas", // "Todas", "Reportada", "Revisada"
    val filtroTipo: String = "Todos",
    val filtroSucursal: String = "Todas", // "Todas", "Centro", "Norte", "Sur"
    val busqueda: String = "",
    val esAdminGeneral: Boolean = true,
    val sucursalUsuario: String = "",
    val mensajeExito: String? = null,
    val mensajeError: String? = null,
    val dialogoRevisionNovedad: Novedad? = null,
    val mostrarDialogoSupervisor: Boolean = false
) {
    val filtroActual: String get() = filtroEstado

    val sucursalesDisponibles: List<String> = listOf("Todas", "Centro", "Norte", "Sur")

    val tiposNovedadFiltro: List<String>
        get() {
            val lista = mutableListOf("Todos")
            val tiposRegistrados = novedades.map { it.tipo }.filter { it.isNotBlank() }.distinct()
            lista.addAll(tiposRegistrados)
            return lista.distinct()
        }

    val empleadosParaSupervisor: List<Empleado>
        get() {
            val activos = empleados.filter { it.estado == EstadoUsuario.ACTIVO }
            return if (esAdminGeneral || sucursalUsuario.isBlank()) {
                activos
            } else {
                activos.filter { it.sucursal.equals(sucursalUsuario, ignoreCase = true) }
            }
        }

    val novedadesFiltradas: List<Novedad>
        get() {
            return novedades.filter { nov ->
                val pasaSucursal = if (!esAdminGeneral && sucursalUsuario.isNotBlank()) {
                    nov.sucursal.isBlank() || nov.sucursal.equals(sucursalUsuario, ignoreCase = true)
                } else if (filtroSucursal != "Todas") {
                    nov.sucursal.equals(filtroSucursal, ignoreCase = true)
                } else {
                    true
                }

                val pasaEstado = when (filtroEstado.lowercase()) {
                    "reportada" -> nov.estado == EstadoNovedad.REPORTADA
                    "revisada" -> nov.estado == EstadoNovedad.REVISADA
                    else -> true
                }

                val pasaTipo = if (filtroTipo == "Todos" || filtroTipo.isBlank()) {
                    true
                } else {
                    nov.tipo.equals(filtroTipo, ignoreCase = true)
                }

                val pasaBusqueda = if (busqueda.isBlank()) {
                    true
                } else {
                    val query = busqueda.trim().lowercase()
                    nov.empleado.lowercase().contains(query) ||
                    nov.descripcion.lowercase().contains(query) ||
                    nov.detalleTurno.lowercase().contains(query) ||
                    nov.sucursal.lowercase().contains(query) ||
                    nov.tipo.lowercase().contains(query)
                }

                pasaSucursal && pasaEstado && pasaTipo && pasaBusqueda
            }
        }

    val pendientesCount: Int
        get() {
            return novedades.count { nov ->
                val pasaSucursal = if (!esAdminGeneral && sucursalUsuario.isNotBlank()) {
                    nov.sucursal.isBlank() || nov.sucursal.equals(sucursalUsuario, ignoreCase = true)
                } else {
                    true
                }
                pasaSucursal && nov.estado == EstadoNovedad.REPORTADA
            }
        }
}

class NovedadesViewModel(
    private val novedadRepository: NovedadRepositoryContract = NovedadRepository,
    private val empleadoRepository: EmpleadoRepositoryContract = EmpleadoRepository,
    private val turnoRepository: TurnoRepositoryContract = TurnoRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NovedadesState())
    val uiState: StateFlow<NovedadesState> = _uiState.asStateFlow()

    init {
        novedadRepository.iniciarSincronizacionFirestore()
        empleadoRepository.iniciarSincronizacionFirestore()
        turnoRepository.iniciarSincronizacionFirestore()

        val usuario = SessionManager.usuarioActivo
        val esAdminGen = usuario == null || usuario.rol == RolUsuario.ADMINISTRADOR_GENERAL
        val suc = usuario?.sucursal ?: ""
        _uiState.update {
            it.copy(
                esAdminGeneral = esAdminGen,
                sucursalUsuario = suc,
                filtroSucursal = if (!esAdminGen && suc.isNotBlank()) suc else "Todas"
            )
        }

        observarDatos()
    }

    private fun observarDatos() {
        viewModelScope.launch {
            combine(
                novedadRepository.novedades,
                empleadoRepository.empleados,
                turnoRepository.asignaciones
            ) { novedades, empleados, asignaciones ->
                Triple(novedades, empleados, asignaciones)
            }.collect { (novedades, empleados, asignaciones) ->
                _uiState.update {
                    it.copy(
                        novedades = novedades,
                        empleados = empleados,
                        asignaciones = asignaciones
                    )
                }
            }
        }
    }

    fun cambiarFiltro(nuevoFiltro: String) {
        _uiState.update { it.copy(filtroEstado = nuevoFiltro) }
    }

    fun cambiarFiltroTipo(nuevoTipo: String) {
        _uiState.update { it.copy(filtroTipo = nuevoTipo) }
    }

    fun cambiarFiltroSucursal(nuevaSucursal: String) {
        _uiState.update { it.copy(filtroSucursal = nuevaSucursal) }
    }

    fun cambiarBusqueda(texto: String) {
        _uiState.update { it.copy(busqueda = texto) }
    }

    fun limpiarMensajes() {
        _uiState.update { it.copy(mensajeExito = null, mensajeError = null) }
    }

    fun abrirDialogoRevision(novedad: Novedad) {
        _uiState.update { it.copy(dialogoRevisionNovedad = novedad) }
    }

    fun cerrarDialogoRevision() {
        _uiState.update { it.copy(dialogoRevisionNovedad = null) }
    }

    fun abrirDialogoSupervisor() {
        _uiState.update { it.copy(mostrarDialogoSupervisor = true, mensajeError = null) }
    }

    fun cerrarDialogoSupervisor() {
        _uiState.update { it.copy(mostrarDialogoSupervisor = false, mensajeError = null) }
    }

    fun marcarComoRevisada(idNovedad: String) {
        val nov = _uiState.value.novedades.find { it.id == idNovedad }
        if (nov != null) {
            abrirDialogoRevision(nov)
        } else {
            revisarNovedadConComentario(idNovedad, "Revisado por administración")
        }
    }

    fun revisarNovedadConComentario(idNovedad: String, comentario: String) {
        val revisor = SessionManager.usuarioActivo?.let {
            if (it.nombre.isNotBlank()) "${it.nombre} (${it.rol.titulo})" else it.rol.titulo
        } ?: "Administrador"

        val hoy = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        novedadRepository.revisarNovedad(
            idNovedad = idNovedad,
            comentario = comentario.ifBlank { "Revisado y conforme" },
            revisor = revisor,
            fecha = hoy
        )
        _uiState.update {
            it.copy(
                dialogoRevisionNovedad = null,
                mensajeExito = "Novedad revisada exitosamente."
            )
        }
    }

    fun registrarNovedadSupervisor(
        empleado: Empleado,
        tipoNovedad: String,
        fechaIso: String,
        horaReal: String,
        descripcion: String
    ): Boolean {
        if (descripcion.isBlank()) {
            _uiState.update { it.copy(mensajeError = "La descripción de la novedad es obligatoria (CA-14.2).") }
            return false
        }

        val hoy = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val fechaObj = try { LocalDate.parse(fechaIso) } catch (_: Throwable) { hoy }
        val diffDias = hoy.toEpochDays() - fechaObj.toEpochDays()

        // CA-14.6: Plazo máximo de 48 horas hacia atrás
        if (diffDias > 2) {
            _uiState.update { it.copy(mensajeError = "No es posible registrar novedades para turnos con más de 48 horas de antigüedad (CA-14.6).") }
            return false
        }

        val empNombreCompleto = if (empleado.apellido.isNotBlank()) {
            "${empleado.nombre} ${empleado.apellido}".trim()
        } else {
            empleado.nombre
        }

        // CA-14.3: Evitar registros duplicados de novedad para el mismo empleado, fecha y tipo
        val yaExiste = _uiState.value.novedades.any { nov ->
            (nov.empleadoId == empleado.id || nov.empleado.equals(empNombreCompleto, ignoreCase = true)) &&
            nov.tipo.equals(tipoNovedad, ignoreCase = true) &&
            (nov.fechaReferencia == fechaIso || nov.detalleTurno.contains(fechaIso))
        }

        if (yaExiste) {
            _uiState.update { it.copy(mensajeError = "Ya existe una novedad registrada de tipo '$tipoNovedad' para este empleado y fecha (CA-14.3).") }
            return false
        }

        // Resolver turno de la fecha si existe en asignaciones
        val asig = _uiState.value.asignaciones.find {
            it.empleadoId == empleado.id && it.fechaIso == fechaIso
        }
        val detalleTurnoTexto = when {
            asig != null && asig.esDescanso -> "Descanso · $fechaIso"
            asig != null && asig.tipoTurno.isNotBlank() -> "${asig.tipoTurno} (${asig.horario.replace("\n", " ").trim()}) · $fechaIso"
            else -> "Turno asignado · $fechaIso"
        }

        val color = when (tipoNovedad) {
            "Ausencia injustificada" -> "ROJO"
            "Ausencia justificada", "Incapacidad médica" -> "ROJO_OSCURO"
            "Llegada tarde", "Salida antes de la hora" -> "MARRON"
            "Ampliación del turno" -> "VERDE"
            else -> "GRIS"
        }

        val diferenciaTexto = if (horaReal.isNotBlank()) "Hora real: $horaReal" else ""

        val nuevaNovedad = Novedad(
            id = "nov_sup_${Clock.System.now().toEpochMilliseconds()}",
            tipo = tipoNovedad,
            descripcion = descripcion.trim(),
            estado = EstadoNovedad.REPORTADA,
            empleado = empNombreCompleto,
            empleadoId = empleado.id,
            sucursal = empleado.sucursal,
            detalleTurno = detalleTurnoTexto,
            diferencia = diferenciaTexto,
            horaReal = horaReal.trim(),
            colorBorde = color,
            colorVorde = color,
            asignacionTurnoId = asig?.id ?: "",
            fechaReferencia = fechaIso,
            fechaCreacionIso = hoy.toString(),
            reportadoPorSupervisor = true
        )

        novedadRepository.reportarNovedad(nuevaNovedad)
        _uiState.update {
            it.copy(
                mostrarDialogoSupervisor = false,
                mensajeError = null,
                mensajeExito = "Novedad registrada exitosamente para $empNombreCompleto."
            )
        }
        return true
    }
}
