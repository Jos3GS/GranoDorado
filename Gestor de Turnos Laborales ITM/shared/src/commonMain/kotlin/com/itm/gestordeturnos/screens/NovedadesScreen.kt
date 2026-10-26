package com.itm.gestordeturnos.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.itm.gestordeturnos.CafeAccentYellow
import com.itm.gestordeturnos.CafeBackgroundLight
import com.itm.gestordeturnos.CafeBorderColor
import com.itm.gestordeturnos.CafeDarkBrown
import com.itm.gestordeturnos.CafeStatusErrorBg
import com.itm.gestordeturnos.CafeStatusErrorText
import com.itm.gestordeturnos.CafeStatusSuccessBg
import com.itm.gestordeturnos.CafeStatusSuccessText
import com.itm.gestordeturnos.CafeStatusWarningBg
import com.itm.gestordeturnos.CafeTextPrimary
import com.itm.gestordeturnos.CafeTextSecondary
import com.itm.gestordeturnos.components.AppDropdown
import com.itm.gestordeturnos.components.EncabezadoPantalla
import com.itm.gestordeturnos.components.FiltroDropdown
import com.itm.gestordeturnos.components.NovedadCard
import com.itm.gestordeturnos.components.SucursalChip
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.Novedad
import com.itm.gestordeturnos.viewmodel.NovedadesViewModel
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

private val OPCIONES_FILTRO_ESTADO = listOf("Todas", "Reportada", "Revisada")

private val TIPOS_NOVEDAD_SUPERVISOR = listOf(
    "Ausencia injustificada",
    "Ausencia justificada",
    "Llegada tarde",
    "Salida antes de la hora",
    "Ampliación del turno",
    "Incapacidad médica",
    "Permiso remunerado",
    "Permiso no remunerado",
    "Otro"
)

@Composable
fun NovedadesScreen(viewModel: NovedadesViewModel) {
    val state by viewModel.uiState.collectAsState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompact = maxWidth < 800.dp

        Column(modifier = Modifier.fillMaxSize()) {
            EncabezadoPantalla(
                titulo = "Bandeja de novedades",
                subtitulo = "Lo que el equipo reporta llega aquí y queda constancia de que fue leído.",
                isCompact = isCompact,
                accion = {
                    Button(
                        onClick = { viewModel.abrirDialogoSupervisor() },
                        colors = ButtonDefaults.buttonColors(containerColor = CafeDarkBrown),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Registrar novedad de empleado", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )

            // Mensajes de Éxito o Error
            if (state.mensajeExito != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CafeStatusSuccessBg, RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(state.mensajeExito ?: "", color = CafeStatusSuccessText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        IconButton(onClick = { viewModel.limpiarMensajes() }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = CafeStatusSuccessText, modifier = Modifier.size(14.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (state.mensajeError != null && !state.mostrarDialogoSupervisor) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CafeStatusErrorBg, RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(state.mensajeError ?: "", color = CafeStatusErrorText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        IconButton(onClick = { viewModel.limpiarMensajes() }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = CafeStatusErrorText, modifier = Modifier.size(14.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Filtros de Sucursal (Solo Administrador General)
            if (state.esAdminGeneral) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Sucursal:", color = CafeTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    state.sucursalesDisponibles.forEach { suc ->
                        val isSelected = state.filtroSucursal.equals(suc, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) CafeDarkBrown else Color.Transparent,
                                    RoundedCornerShape(14.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) CafeDarkBrown else CafeBorderColor,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { viewModel.cambiarFiltroSucursal(suc) }
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = suc,
                                color = if (isSelected) Color.White else CafeTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Barra de Búsqueda y Filtros de Estado / Tipo
            if (isCompact) {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    OutlinedTextField(
                        value = state.busqueda,
                        onValueChange = { viewModel.cambiarBusqueda(it) },
                        placeholder = { Text("Buscar empleado, turno o motivo...", fontSize = 12.sp, color = CafeTextSecondary) },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CafeDarkBrown,
                            unfocusedBorderColor = CafeBorderColor
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FiltroDropdown(
                            opcionSeleccionada = state.filtroEstado,
                            opciones = OPCIONES_FILTRO_ESTADO,
                            onOpcionSeleccionada = { viewModel.cambiarFiltro(it) },
                            modifier = Modifier.weight(1f)
                        )
                        FiltroDropdown(
                            opcionSeleccionada = state.filtroTipo,
                            opciones = state.tiposNovedadFiltro,
                            onOpcionSeleccionada = { viewModel.cambiarFiltroTipo(it) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.busqueda,
                        onValueChange = { viewModel.cambiarBusqueda(it) },
                        placeholder = { Text("Buscar novedad por empleado, turno o descripción...", fontSize = 13.sp, color = CafeTextSecondary) },
                        modifier = Modifier.weight(1f).height(46.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CafeDarkBrown,
                            unfocusedBorderColor = CafeBorderColor
                        )
                    )
                    FiltroDropdown(
                        opcionSeleccionada = state.filtroEstado,
                        opciones = OPCIONES_FILTRO_ESTADO,
                        onOpcionSeleccionada = { viewModel.cambiarFiltro(it) }
                    )
                    FiltroDropdown(
                        opcionSeleccionada = state.filtroTipo,
                        opciones = state.tiposNovedadFiltro,
                        onOpcionSeleccionada = { viewModel.cambiarFiltroTipo(it) }
                    )
                }
            }

            // Contador de Novedades Pendientes
            Box(
                modifier = Modifier
                    .background(CafeStatusWarningBg, RoundedCornerShape(2.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    "${state.pendientesCount} NOVEDADES PENDIENTES DE REVISIÓN",
                    color = CafeDarkBrown,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de Novedades
            if (state.novedadesFiltradas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No se encontraron novedades con los filtros seleccionados.",
                        color = CafeTextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(bottom = 32.dp)) {
                    items(state.novedadesFiltradas, key = { it.id }) { novedad ->
                        NovedadCard(
                            novedad = novedad,
                            onMarcarRevisada = { viewModel.abrirDialogoRevision(novedad) }
                        )
                    }
                }
            }
        }

        // Diálogo de Revisión con Comentario (CA-12.3)
        state.dialogoRevisionNovedad?.let { novedad ->
            DialogoRevisarNovedad(
                novedad = novedad,
                onConfirmar = { comentario ->
                    viewModel.revisarNovedadConComentario(novedad.id, comentario)
                },
                onDismiss = { viewModel.cerrarDialogoRevision() }
            )
        }

        // Diálogo de Registro de Novedad por Supervisor (HU-14)
        if (state.mostrarDialogoSupervisor) {
            DialogoRegistrarNovedadSupervisor(
                empleados = state.empleadosParaSupervisor,
                mensajeError = state.mensajeError,
                onRegistrar = { emp, tipo, fecha, horaReal, desc ->
                    viewModel.registrarNovedadSupervisor(emp, tipo, fecha, horaReal, desc)
                },
                onDismiss = { viewModel.cerrarDialogoSupervisor() }
            )
        }
    }
}

@Composable
private fun DialogoRevisarNovedad(
    novedad: Novedad,
    onConfirmar: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var comentario by remember { mutableStateOf("") }
    val isIncapacidad = novedad.tipo.contains("incapacidad", ignoreCase = true)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f).padding(16.dp),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, CafeBorderColor)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isIncapacidad) "Aprobar incapacidad médica" else "Revisar novedad",
                        color = CafeDarkBrown,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = CafeTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Detalle de la novedad
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CafeBackgroundLight, RoundedCornerShape(4.dp))
                        .padding(12.dp)
                ) {
                    Text("Empleado: ${novedad.empleado}", color = CafeTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Tipo: ${novedad.tipo}", color = CafeDarkBrown, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    if (novedad.detalleTurno.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Turno: ${novedad.detalleTurno}", color = CafeTextSecondary, fontSize = 12.sp)
                    }
                    if (novedad.diferencia.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(novedad.diferencia, color = Color(0xFF333333), fontSize = 12.sp)
                    }
                    if (novedad.descripcion.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Motivo reportado: ${novedad.descripcion}", color = CafeTextSecondary, fontSize = 12.sp)
                    }
                }

                if (isIncapacidad) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFEF2F2), RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(4.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            "Al aprobar esta novedad, el turno del empleado será reemplazado automáticamente por 'Incapacidad' bloqueada.",
                            color = Color(0xFF991B1B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("OBSERVACIÓN / COMENTARIO DE REVISIÓN", color = CafeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = comentario,
                    onValueChange = { comentario = it },
                    placeholder = { Text(if (isIncapacidad) "Ej. Soporte EPS validado y conforme." else "Ej. Revisado y justificación aceptada.") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CafeDarkBrown,
                        unfocusedBorderColor = CafeBorderColor
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = CafeTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirmar(comentario) },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isIncapacidad) Color(0xFF991B1B) else CafeDarkBrown),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(if (isIncapacidad) "Aprobar incapacidad" else "Confirmar revisión")
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogoRegistrarNovedadSupervisor(
    empleados: List<Empleado>,
    mensajeError: String?,
    onRegistrar: (Empleado, String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val hoy = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
    var empleadoSeleccionado by remember { mutableStateOf(empleados.firstOrNull()) }
    var tipoSeleccionado by remember { mutableStateOf(TIPOS_NOVEDAD_SUPERVISOR.first()) }
    var fechaIso by remember { mutableStateOf(hoy) }
    var horaReal by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    val requiereHoraReal = tipoSeleccionado in listOf("Llegada tarde", "Salida antes de la hora", "Ampliación del turno")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, CafeBorderColor)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Registrar novedad por supervisor",
                        color = CafeDarkBrown,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = CafeTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Permite registrar ausencias, retrasos u otras novedades a nombre de un empleado (HU-14).",
                    color = CafeTextSecondary,
                    fontSize = 12.sp
                )

                if (mensajeError != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CafeStatusErrorBg, RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        Text(mensajeError, color = CafeStatusErrorText, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Selector de Empleado
                Text("EMPLEADO", color = CafeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(4.dp))
                if (empleados.isEmpty()) {
                    Text("No hay empleados disponibles en tu sucursal.", color = CafeStatusErrorText, fontSize = 12.sp)
                } else {
                    AppDropdown(
                        valorActual = empleadoSeleccionado ?: empleados.first(),
                        opciones = empleados,
                        onSeleccion = { empleadoSeleccionado = it },
                        formatoTexto = { emp ->
                            val nombre = if (emp.apellido.isNotBlank()) "${emp.nombre} ${emp.apellido}" else emp.nombre
                            "$nombre (${emp.sucursal})"
                        },
                        altura = 42.dp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Selector de Tipo de Novedad
                Text("TIPO DE NOVEDAD", color = CafeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(4.dp))
                AppDropdown(
                    valorActual = tipoSeleccionado,
                    opciones = TIPOS_NOVEDAD_SUPERVISOR,
                    onSeleccion = { tipoSeleccionado = it },
                    altura = 42.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Fecha del Turno (ISO)
                Text("FECHA DEL TURNO (YYYY-MM-DD)", color = CafeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = fechaIso,
                    onValueChange = { fechaIso = it },
                    placeholder = { Text("YYYY-MM-DD") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CafeDarkBrown,
                        unfocusedBorderColor = CafeBorderColor
                    )
                )

                // Hora Real si aplica
                if (requiereHoraReal) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("HORA REAL (HH:mm)", color = CafeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = horaReal,
                        onValueChange = { horaReal = it },
                        placeholder = { Text("Ej. 14:45") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CafeDarkBrown,
                            unfocusedBorderColor = CafeBorderColor
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Descripción Obligatoria
                Text("MOTIVO / DESCRIPCIÓN (OBLIGATORIO)", color = CafeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    placeholder = { Text("Indica la justificación o causa de la novedad...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CafeDarkBrown,
                        unfocusedBorderColor = CafeBorderColor
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = CafeTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val emp = empleadoSeleccionado ?: empleados.firstOrNull()
                            if (emp != null) {
                                onRegistrar(emp, tipoSeleccionado, fechaIso, horaReal, descripcion)
                            }
                        },
                        enabled = empleados.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = CafeDarkBrown),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Registrar novedad")
                    }
                }
            }
        }
    }
}
