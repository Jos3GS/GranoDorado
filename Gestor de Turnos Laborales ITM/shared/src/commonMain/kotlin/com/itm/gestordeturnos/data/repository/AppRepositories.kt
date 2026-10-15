package com.itm.gestordeturnos.data.repository

import com.itm.gestordeturnos.data.mock.MockDataProviders
import com.itm.gestordeturnos.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object EmpleadoRepository {
    private val _empleados = MutableStateFlow(MockDataProviders.inicialEmpleados())
    val empleados: StateFlow<List<Empleado>> = _empleados.asStateFlow()

    fun guardarEmpleado(empleado: Empleado, esNuevo: Boolean) {
        _empleados.update { lista ->
            if (esNuevo) {
                lista + empleado
            } else {
                lista.map { if (it.id == empleado.id) empleado else it }
            }
        }
    }
}

object TurnoRepository {
    private val _turnosCatalogo = MutableStateFlow(MockDataProviders.inicialTurnosCatalogo())
    val turnosCatalogo: StateFlow<List<TurnoTemplate>> = _turnosCatalogo.asStateFlow()

    private val _horarioSemana = MutableStateFlow(MockDataProviders.inicialHorarioSemana())
    val horarioSemana: StateFlow<List<TurnoAsignado>> = _horarioSemana.asStateFlow()

    private val _filasMalla = MutableStateFlow(MockDataProviders.inicialFilasMalla())
    val filasMalla: StateFlow<List<FilaMalla>> = _filasMalla.asStateFlow()

    private val _turnosPropios = MutableStateFlow(MockDataProviders.inicialTurnosPropios())
    val turnosPropios: StateFlow<List<TurnoSeleccionable>> = _turnosPropios.asStateFlow()

    private val _turnosCompaneros = MutableStateFlow(MockDataProviders.inicialTurnosCompaneros())
    val turnosCompaneros: StateFlow<List<TurnoCompaneroSeleccionable>> = _turnosCompaneros.asStateFlow()

    fun agregarTurnoCatalogo(turno: TurnoTemplate) {
        _turnosCatalogo.update { it + turno }
    }

    fun getTurnoHoy(): TurnoAsignado? {
        // Retorna el turno de hoy de forma dinámica según la lista semanal
        return _horarioSemana.value.firstOrNull { !it.esDescanso && !it.sinAsignar }
    }
}

object NovedadRepository {
    private val _novedades = MutableStateFlow(MockDataProviders.inicialNovedades())
    val novedades: StateFlow<List<Novedad>> = _novedades.asStateFlow()

    fun reportarNovedad(novedad: Novedad) {
        _novedades.update { listOf(novedad) + it }
    }
}

object CambiosTurnoRepository {
    private val _solicitudes = MutableStateFlow(MockDataProviders.inicialSolicitudesCambio())
    val solicitudes: StateFlow<List<SolicitudCambio>> = _solicitudes.asStateFlow()

    fun solicitarCambio(solicitud: SolicitudCambio) {
        _solicitudes.update { listOf(solicitud) + it }
    }
}

object AsistenciaRepository {
    private val _notificaciones = MutableStateFlow(MockDataProviders.inicialIncumplimientos())
    val notificaciones: StateFlow<List<NotificacionIncumplimiento>> = _notificaciones.asStateFlow()

    private val _registrosDiarios = MutableStateFlow(MockDataProviders.inicialRegistrosAsistencia())
    val registrosDiarios: StateFlow<List<RegistroAsistencia>> = _registrosDiarios.asStateFlow()
}
