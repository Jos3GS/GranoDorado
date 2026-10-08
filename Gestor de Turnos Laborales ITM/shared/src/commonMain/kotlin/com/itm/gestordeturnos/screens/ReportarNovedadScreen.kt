package com.itm.gestordeturnos.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itm.gestordeturnos.CafeDarkBrown
import com.itm.gestordeturnos.CafeTextPrimary
import com.itm.gestordeturnos.CafeTextSecondary
import com.itm.gestordeturnos.components.NovedadCard
import com.itm.gestordeturnos.viewmodel.ReportarNovedadViewModel
import androidx.compose.material.icons.filled.DateRange // Icono de calendario
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api // Requerido para DatePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.autofill.ContentDataType.Companion.Date
import androidx.compose.ui.text.input.KeyboardType.Companion.Date
import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportarNovedadScreen(viewModel: ReportarNovedadViewModel) {
    val state by viewModel.uiState.collectAsState()

    var rowSize by remember { mutableStateOf(IntSize.Zero) }

    // Permite scroll general en pantallas pequeñas
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(bottom = 32.dp)
    ) {

        // 1. ENCABEZADO
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {

                Text("Reportar novedad", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = CafeTextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Deja constancia de la diferencia entre el turno programado y el turno trabajado.",
                    fontSize = 14.sp,
                    color = CafeTextSecondary
                )
            }
        }

        // 2. FORMULARIO (Tarjeta principal)
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White), // O el fondo de tu app
                border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                shape = RoundedCornerShape(2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    // A. Turno de referencia
                    val permiteCambiarFecha = state.tipoNovedad == "Incapacidad médica" || state.tipoNovedad == "Permiso especial"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("TURNO DE REFERENCIA", color = CafeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(state.turnoReferencia, color = CafeTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        // Si permite cambiar fecha, lo volvemos un botón clickeable con icono
                        if (permiteCambiarFecha) {
                            Row(
                                modifier = Modifier
                                    .clickable { viewModel.toggleDatePicker(true) }
                                    .border(1.dp, CafeDarkBrown, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(state.fechaReferencia, color = CafeDarkBrown, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.DateRange, contentDescription = "Cambiar fecha", tint = CafeDarkBrown, modifier = Modifier.size(16.dp))
                            }
                        } else {
                            // Si no, se queda como texto estático
                            Text(state.fechaReferencia, color = CafeTextSecondary, fontSize = 14.sp)
                        }
                    }

                    // MODAL DEL CALENDARIO (Solo se dibuja si mostrarDatePicker es true)
                    if (state.mostrarDatePicker) {
                        val datePickerState = rememberDatePickerState()

                        DatePickerDialog(
                            onDismissRequest = { viewModel.toggleDatePicker(false) },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        datePickerState.selectedDateMillis?.let { millis ->
                                            val instant = Instant.fromEpochMilliseconds(millis)
                                            val fechaFormateada = instant.toLocalDateTime(TimeZone.UTC).date.toString()

                                            viewModel.onFechaReferenciaCambiada(fechaFormateada)
                                        } ?: viewModel.toggleDatePicker(false)
                                    }
                                ) {
                                    Text("Aceptar", color = CafeDarkBrown)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { viewModel.toggleDatePicker(false) }) {
                                    Text("Cancelar", color = CafeTextSecondary)
                                }
                            }
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }
                    // FIN DEL MODAL DEL CALENDARIO
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFE0E0E0))
                    Spacer(modifier = Modifier.height(16.dp))

                    // B. Desplegable: Tipo de Novedad
                    Text("TIPO DE NOVEDAD", color = CafeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onSizeChanged { rowSize = it } // 1. CAPTURAMOS EL TAMAÑO DE LA CAJA AQUÍ
                                .background(Color(0xFFF9F9F9))
                                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(2.dp))
                                .clickable { viewModel.toggleMenu(true) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = state.tipoNovedad,
                                color = if (state.tipoNovedad == "Selecciona...") CafeTextSecondary else CafeTextPrimary,
                                fontSize = 14.sp
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Seleccionar", tint = CafeTextSecondary)
                        }

                        DropdownMenu(
                            expanded = state.menuExpandido,
                            onDismissRequest = { viewModel.toggleMenu(false) },
                            modifier = Modifier
                                .background(Color.White)
                                // 2. APLICAMOS EL ANCHO EXACTO AL MENÚ
                                .width(with(LocalDensity.current) { rowSize.width.toDp() })
                        ) {
                            state.opcionesNovedad.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion, color = CafeTextPrimary) },
                                    onClick = { viewModel.onTipoNovedadSeleccionado(opcion) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // C. Campo de texto: Descripción
                    Text("DESCRIPCIÓN", color = CafeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state.descripcion,
                        onValueChange = { viewModel.onDescripcionCambiada(it) },
                        placeholder = { Text("Describe qué ocurrió durante tu turno...", color = CafeTextSecondary, fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(2.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF9F9F9),
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedBorderColor = CafeDarkBrown
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // D. Botón Enviar
                    Button(
                        onClick = { viewModel.enviarFormulario() },
                        colors = ButtonDefaults.buttonColors(containerColor = CafeDarkBrown),
                        shape = RoundedCornerShape(2.dp)
                    ) {
                        Text("Enviar", fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }

        // 3. HISTORIAL DE NOVEDADES
        item {
            Text(
                "MIS NOVEDADES REPORTADAS",
                color = CafeTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Iteramos sobre las novedades utilizando el componente NovedadCard
        items(state.historial) { novedad ->
            NovedadCard(novedad = novedad)
        }
    }
}