package com.itm.gestordeturnos.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.itm.gestordeturnos.*
import com.itm.gestordeturnos.components.EncabezadoPantalla
import com.itm.gestordeturnos.components.SucursalChip
import com.itm.gestordeturnos.domain.model.NotificacionIncumplimiento
import com.itm.gestordeturnos.domain.model.RegistroAsistencia
import com.itm.gestordeturnos.viewmodel.AsistenciaViewModel
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun AsistenciaScreen(viewModel: AsistenciaViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var notificacionAAtender by remember { mutableStateOf<NotificacionIncumplimiento?>(null) }
    var registroAMarcar by remember { mutableStateOf<RegistroAsistencia?>(null) }
    var feedbackMensaje by remember { mutableStateOf<String?>(null) }

    // Scroll vertical general de la pantalla
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {

        // 1. ENCABEZADO
        EncabezadoPantalla(
            titulo = "Control de asistencia",
            subtitulo = if (state.fechaHoyTexto.isNotBlank()) "Jornada: ${state.fechaHoyTexto} · Estado en tiempo real." else "Estado de las marcaciones del día y notificaciones de incumplimientos."
        )

        // FILTRO DE SUCURSAL PARA ADMINISTRADOR GENERAL
        if (state.esAdminGeneral) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filtrar por sucursal:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CafeTextSecondary)
                state.sucursalesDisponibles.forEach { sucursal ->
                    val seleccionada = state.sucursalFiltro.equals(sucursal, ignoreCase = true)
                    val bg = if (seleccionada) CafeDarkBrown else Color.White
                    val txtColor = if (seleccionada) Color.White else CafeTextPrimary
                    val border = if (seleccionada) CafeDarkBrown else CafeBorderLight
                    OutlinedButton(
                        onClick = { viewModel.cambiarSucursalFiltro(sucursal) },
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = bg),
                        border = BorderStroke(1.dp, border),
                        shape = RoundedCornerShape(2.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(sucursal, color = txtColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. PANEL DE INCUMPLIMIENTOS (Borde izquierdo rojo oscuro)
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = CafeSurfaceWarm),
            border = BorderStroke(1.dp, CafeBorderLight),
            shape = RoundedCornerShape(2.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                // Línea roja indicadora lateral
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF9E3422))
                )

                Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                    Text(
                        "NOTIFICACIONES DE INCUMPLIMIENTO",
                        color = CafeTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    feedbackMensaje?.let { msg ->
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).background(CafeStatusSuccessBg, RoundedCornerShape(2.dp)).padding(8.dp)
                        ) {
                            Text(msg, color = CafeStatusSuccessText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    if (state.notificaciones.isEmpty()) {
                        Text(
                            text = "No se registran incumplimientos en la jornada de hoy.",
                            color = CafeTextSecondary,
                            fontSize = 14.sp
                        )
                    } else {
                        state.notificaciones.forEach { notificacion ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    val dotColor = if (notificacion.atendida) Color(0xFF2E7D32) else Color(0xFF9E3422)
                                    Box(modifier = Modifier.size(8.dp).background(dotColor, CircleShape))
                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = notificacion.empleadoNombre.ifBlank { notificacion.empleado },
                                                fontWeight = FontWeight.Bold,
                                                color = CafeTextPrimary,
                                                fontSize = 14.sp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            SucursalChip(sucursal = notificacion.sucursal)
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${notificacion.tipo} · ${notificacion.detalleHora}".trim(),
                                            color = CafeTextSecondary,
                                            fontSize = 13.sp
                                        )
                                        if (notificacion.atendida && notificacion.motivoAtencion.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Constancia: ${notificacion.motivoAtencion} (${notificacion.horaAtencion})",
                                                color = Color(0xFF2E7D32),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                if (!notificacion.atendida) {
                                    OutlinedButton(
                                        onClick = { notificacionAAtender = notificacion },
                                        border = BorderStroke(1.dp, CafeDarkBrown),
                                        shape = RoundedCornerShape(2.dp),
                                        modifier = Modifier.height(30.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text("Dejar constancia", color = CafeDarkBrown, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFE8F5E9), RoundedCornerShape(2.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("ATENDIDA", color = Color(0xFF2E7D32), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. TABLA DE ASISTENCIA
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isDesktop = maxWidth > 800.dp

            if (isDesktop) {
                TablaAsistencia(
                    registros = state.registrosDiarios,
                    onRegistrarMarca = { registroAMarcar = it },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    TablaAsistencia(
                        registros = state.registrosDiarios,
                        onRegistrarMarca = { registroAMarcar = it },
                        modifier = Modifier.width(900.dp)
                    )
                }
            }
        }

        // DIÁLOGOS MODALES
        notificacionAAtender?.let { notif ->
            DialogoAtenderIncumplimiento(
                notificacion = notif,
                onDismiss = { notificacionAAtender = null },
                onConfirm = { motivo ->
                    viewModel.atenderIncumplimiento(notif.id, motivo)
                    notificacionAAtender = null
                    feedbackMensaje = "Constancia guardada con éxito para ${notif.empleadoNombre.ifBlank { notif.empleado }}."
                }
            )
        }

        registroAMarcar?.let { reg ->
            DialogoRegistrarMarcaSupervisor(
                registro = reg,
                onDismiss = { registroAMarcar = null },
                onConfirm = { tipo, hora, motivo ->
                    viewModel.registrarMarcaSupervisor(
                        registroId = reg.id,
                        empleadoId = reg.empleadoId,
                        empleadoNombre = reg.empleado,
                        sucursal = reg.sucursal,
                        fechaIso = reg.fechaIso,
                        tipoTurno = reg.tipoTurno,
                        horarioProg = reg.horarioProgramado,
                        tipoMarca = tipo,
                        horaMarca = hora,
                        motivo = motivo
                    ) { exito, err ->
                        if (exito) {
                            feedbackMensaje = "Marca ($tipo) registrada con éxito para ${reg.empleado}."
                        } else {
                            feedbackMensaje = err ?: "Error al registrar la marcación."
                        }
                    }
                    registroAMarcar = null
                }
            )
        }
    }
}

// COMPONENTES DE LA TABLA
@Composable
private fun TablaAsistencia(registros: List<RegistroAsistencia>, onRegistrarMarca: (RegistroAsistencia) -> Unit, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CafeBorderLight),
        shape = RoundedCornerShape(2.dp),
        modifier = modifier
    ) {
        Column {
            // Encabezados
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CabeceraTablaAsis("EMPLEADO", modifier = Modifier.weight(2f))
                CabeceraTablaAsis("SUCURSAL", modifier = Modifier.weight(1.5f))
                CabeceraTablaAsis("ENTRADA", modifier = Modifier.weight(1.5f))
                CabeceraTablaAsis("SALIDA", modifier = Modifier.weight(1.5f))
                CabeceraTablaAsis("ESTADO", modifier = Modifier.weight(1.5f))
                CabeceraTablaAsis("ACCIONES", modifier = Modifier.weight(1.5f))
            }
            HorizontalDivider(color = CafeBorderLight)

            // Filas
            if (registros.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No hay registros de asistencia para la jornada de hoy.",
                        color = CafeTextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                registros.forEach { registro ->
                    FilaAsistencia(registro = registro, onRegistrarMarca = onRegistrarMarca)
                    HorizontalDivider(color = CafeBorderLight)
                }
            }
        }
    }
}

@Composable
private fun CabeceraTablaAsis(texto: String, modifier: Modifier) {
    Text(
        text = texto,
        modifier = modifier,
        color = CafeTextSecondary,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
}

@Composable
private fun FilaAsistencia(registro: RegistroAsistencia, onRegistrarMarca: (RegistroAsistencia) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Columna Empleado
        Column(modifier = Modifier.weight(2f)) {
            Text(registro.empleado, color = CafeTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            if (registro.registradoPorSupervisor) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .background(CafeAccentYellow.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Por supervisor", color = CafeDarkBrown, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Columna Sucursal
        Box(modifier = Modifier.weight(1.5f)) {
            SucursalChip(sucursal = registro.sucursal)
        }

        // Columna Entrada
        Text(registro.horaEntrada, modifier = Modifier.weight(1.5f), color = CafeTextPrimary, fontSize = 14.sp)

        // Columna Salida
        Text(registro.horaSalida, modifier = Modifier.weight(1.5f), color = CafeTextPrimary, fontSize = 14.sp)

        // Columna Estado (Tokens semánticos de Theme)
        Box(modifier = Modifier.weight(1.5f)) {
            val bgEstado = when (registro.estado) {
                "PRESENTE" -> CafeStatusSuccessBg
                "TARDE" -> CafeStatusWarningBg
                "EN DESCANSO" -> CafeStatusInfoBg
                "FINALIZADO" -> Color(0xFFF0F0F0)
                "PENDIENTE" -> Color(0xFFF5F5F5)
                else -> CafeStatusErrorBg
            }
            val textEstado = when (registro.estado) {
                "PRESENTE" -> CafeStatusSuccessText
                "TARDE" -> CafeStatusWarningText
                "EN DESCANSO" -> CafeStatusInfoText
                "FINALIZADO" -> CafeTextSecondary
                "PENDIENTE" -> CafeTextSecondary
                else -> CafeStatusErrorText
            }

            Box(modifier = Modifier.background(bgEstado, RoundedCornerShape(2.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(registro.estado, color = textEstado, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }

        // Columna Acciones
        Box(modifier = Modifier.weight(1.5f)) {
            OutlinedButton(
                onClick = { onRegistrarMarca(registro) },
                border = BorderStroke(1.dp, CafeBorderLight),
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier.height(28.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text("+ Marca", color = CafeTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DialogoAtenderIncumplimiento(
    notificacion: NotificacionIncumplimiento,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var motivo by remember { mutableStateOf("") }
    var errorMotivo by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Atender Incumplimiento", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CafeTextPrimary)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Empleado: ${notificacion.empleadoNombre.ifBlank { notificacion.empleado }}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = CafeTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Situación: ${notificacion.tipo}",
                    fontSize = 13.sp,
                    color = CafeTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notificacion.detalleHora,
                    fontSize = 12.sp,
                    color = CafeTextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Motivo / Constancia de atención (Obligatorio):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = motivo,
                    onValueChange = {
                        motivo = it
                        if (it.isNotBlank()) errorMotivo = false
                    },
                    placeholder = { Text("Ej. Se contactó vía telefónica, informa congestión vehicular.") },
                    isError = errorMotivo,
                    supportingText = if (errorMotivo) { { Text("El motivo es obligatorio", color = Color(0xFFC62828)) } } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(2.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (motivo.isBlank()) {
                        errorMotivo = true
                    } else {
                        onConfirm(motivo.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CafeDarkBrown),
                shape = RoundedCornerShape(2.dp)
            ) {
                Text("Guardar constancia", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, CafeBorderLight),
                shape = RoundedCornerShape(2.dp)
            ) {
                Text("Cancelar", color = CafeTextPrimary)
            }
        }
    )
}

@Composable
private fun DialogoRegistrarMarcaSupervisor(
    registro: RegistroAsistencia,
    onDismiss: () -> Unit,
    onConfirm: (tipoMarca: String, horaMarca: String, motivo: String) -> Unit
) {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val horaActualSugerida = "${now.hour.toString().padStart(2, '0')}:${now.minute.toString().padStart(2, '0')}"

    var tipoMarca by remember {
        mutableStateOf(
            when {
                registro.horaEntrada == "-" -> "ENTRADA"
                registro.inicioDescanso == "-" -> "INICIO_DESCANSO"
                registro.finDescanso == "-" -> "FIN_DESCANSO"
                else -> "SALIDA"
            }
        )
    }
    var horaMarca by remember { mutableStateOf(horaActualSugerida) }
    var motivo by remember { mutableStateOf("") }
    var errorMotivo by remember { mutableStateOf(false) }

    val opciones = listOf(
        "ENTRADA" to "Entrada (Inicio jornada)",
        "INICIO_DESCANSO" to "Inicio de descanso",
        "FIN_DESCANSO" to "Regreso de descanso",
        "SALIDA" to "Salida (Fin jornada)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Registrar marcación por supervisor", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CafeTextPrimary)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Empleado: ${registro.empleado} · Sucursal: ${registro.sucursal}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CafeTextPrimary)
                Text("Horario programado: ${registro.horarioProgramado}", fontSize = 12.sp, color = CafeTextSecondary)
                Spacer(modifier = Modifier.height(14.dp))

                Text("Tipo de marcación a registrar:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CafeTextPrimary)
                Spacer(modifier = Modifier.height(4.dp))

                opciones.forEach { (clave, label) ->
                    val yaRegistradoPorEmpleado = when (clave) {
                        "ENTRADA" -> registro.horaEntrada != "-" && !registro.registradoPorSupervisor
                        "INICIO_DESCANSO" -> registro.inicioDescanso != "-" && !registro.registradoPorSupervisor
                        "FIN_DESCANSO" -> registro.finDescanso != "-" && !registro.registradoPorSupervisor
                        "SALIDA" -> registro.horaSalida != "-" && !registro.registradoPorSupervisor
                        else -> false
                    }
                    val disabled = yaRegistradoPorEmpleado
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                    ) {
                        RadioButton(
                            selected = tipoMarca == clave,
                            onClick = { if (!disabled) tipoMarca = clave },
                            enabled = !disabled
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val textoOpcion = if (disabled) "$label (Ya registrada)" else label
                        Text(
                            text = textoOpcion,
                            fontSize = 12.sp,
                            color = if (disabled) CafeTextSecondary.copy(alpha = 0.5f) else CafeTextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Hora de la marcación (HH:mm):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CafeTextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = horaMarca,
                    onValueChange = { horaMarca = it },
                    placeholder = { Text("HH:mm") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(2.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Motivo / Justificación (Obligatorio CA-16.3):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CafeTextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = motivo,
                    onValueChange = {
                        motivo = it
                        if (it.isNotBlank()) errorMotivo = false
                    },
                    placeholder = { Text("Ej. Falla en lector biométrico / Olvido de registro") },
                    isError = errorMotivo,
                    supportingText = if (errorMotivo) { { Text("El motivo es obligatorio para el supervisor", color = Color(0xFFC62828)) } } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(2.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (motivo.isBlank()) {
                        errorMotivo = true
                    } else {
                        onConfirm(tipoMarca, horaMarca.trim(), motivo.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CafeDarkBrown),
                shape = RoundedCornerShape(2.dp)
            ) {
                Text("Guardar marcación", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, CafeBorderLight),
                shape = RoundedCornerShape(2.dp)
            ) {
                Text("Cancelar", color = CafeTextPrimary)
            }
        }
    )
}
