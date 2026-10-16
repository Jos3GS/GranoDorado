package com.itm.gestordeturnos.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

interface Autenticable {
    fun iniciarSesion(correo: String, clave: String): Boolean
    fun cerrarSesion()
}

@Serializable
abstract class Usuario : Autenticable {
    abstract val id: String
    abstract val nombre: String
    abstract val correo: String
    abstract val claveHash: String
    abstract val estado: EstadoUsuario
    abstract val fechaRegistro: LocalDate?

    fun cambiarClave(nuevaClave: String) {
        // Lógica de cambio de clave
    }

    fun actualizarPerfil(nuevoNombre: String) {
        // Lógica de actualización de perfil
    }

    override fun iniciarSesion(correo: String, clave: String): Boolean {
        return this.correo.equals(correo, ignoreCase = true) && this.estado == EstadoUsuario.ACTIVO
    }

    override fun cerrarSesion() {
        // Lógica de cierre de sesión
    }
}

@Serializable
data class Empleado(
    override val id: String = "",
    override val nombre: String = "",
    val apellido: String = "",
    val documento: Long = 0L,
    val tipoDocumento: String = "CC",
    override val correo: String = "",
    val telefono: String = "",
    override val claveHash: String = "",
    val cargo: Cargo = Cargo.BARISTA,
    val estadoLaboral: EstadoEmpleado = EstadoEmpleado.DISPONIBLE,
    override val estado: EstadoUsuario = EstadoUsuario.ACTIVO,
    override val fechaRegistro: LocalDate? = null,
    val sucursalId: String = "CENTRO",
    val sucursal: String = "CENTRO",
    val fechaIngreso: String = "15/01/2024",
    val iniciales: String = ""
) : Usuario() {

    fun consultarTurnos(asignaciones: List<AsignacionTurno> = emptyList()): List<AsignacionTurno> {
        return asignaciones.filter { it.empleadoId == id }
    }

    fun solicitarPermiso(motivo: String): Ausencia {
        return Ausencia(
            tipo = TipoAusencia.PERMISO,
            motivo = motivo,
            empleadoId = id
        )
    }

    fun peticionCambioTurno(turno: AsignacionTurno): SolicitudCambioTurno {
        return SolicitudCambioTurno(
            empleadoSolicitanteId = id,
            turnoEntregaId = turno.id
        )
    }

    fun reportarNovedad(turno: AsignacionTurno, tipo: String = "General", descripcion: String = ""): Novedad {
        return Novedad(
            tipo = tipo,
            descripcion = descripcion,
            empleadoId = id,
            asignacionTurnoId = turno.id
        )
    }

    fun marcarAsistencia(turno: AsignacionTurno, tipo: TipoMarca = TipoMarca.INICIO_TURNO): MarcaAsistencia {
        return MarcaAsistencia(
            tipo = tipo,
            asignacionTurnoId = turno.id,
            empleadoId = id
        )
    }
}

@Serializable
data class Administrador(
    override val id: String = "",
    override val nombre: String = "",
    override val correo: String = "",
    override val claveHash: String = "",
    override val estado: EstadoUsuario = EstadoUsuario.ACTIVO,
    override val fechaRegistro: LocalDate? = null,
    val tipoAdmin: TipoAdministrador = TipoAdministrador.ADMINISTRADOR_GENERAL,
    val sucursalId: String = "CENTRO"
) : Usuario() {

    fun registrarEmpleado(datos: Empleado): Empleado {
        return datos.copy(estado = EstadoUsuario.ACTIVO)
    }

    fun crearTurno(datos: Turno): Turno {
        return datos.copy(estado = EstadoTurno.ACTIVO)
    }

    fun modificarTurno(turno: Turno): Turno {
        return turno
    }

    fun asignarEmpleado(emp: Empleado, turno: Turno, fecha: LocalDate): AsignacionTurno {
        return AsignacionTurno(
            fecha = fecha,
            empleadoId = emp.id,
            turnoId = turno.id,
            turno = turno
        )
    }

    fun consultarSolicitudes(solicitudes: List<SolicitudCambioTurno> = emptyList()): List<SolicitudCambioTurno> {
        return solicitudes
    }

    fun aprobarSolicitud(sol: SolicitudCambioTurno): SolicitudCambioTurno {
        return sol.aprobar()
    }

    fun rechazarSolicitud(sol: SolicitudCambioTurno, motivo: String): SolicitudCambioTurno {
        return sol.rechazar(motivo)
    }

    fun publicarProgramacion(prog: ProgramacionSemanal): ProgramacionSemanal {
        return prog.publicar()
    }

    fun tramitarAusencia(aus: Ausencia): Ausencia {
        return aus.darTramite()
    }

    fun completarMarcaAsistencia(turno: AsignacionTurno, motivo: String): MarcaAsistencia {
        return MarcaAsistencia(
            asignacionTurnoId = turno.id,
            registradaPorAdministrador = true,
            motivoRegistroManual = motivo
        )
    }

    fun registrarNovedadEmpleado(turno: AsignacionTurno, descripcion: String): Novedad {
        return Novedad(
            descripcion = descripcion,
            asignacionTurnoId = turno.id,
            estado = EstadoNovedad.REVISADA
        )
    }
}
