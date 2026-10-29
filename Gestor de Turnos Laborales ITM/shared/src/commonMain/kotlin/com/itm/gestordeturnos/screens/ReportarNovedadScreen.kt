package com.itm.gestordeturnos.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.itm.gestordeturnos.*
import com.itm.gestordeturnos.components.AppDropdown
import com.itm.gestordeturnos.components.EncabezadoPantalla
import com.itm.gestordeturnos.components.NovedadCard
import com.itm.gestordeturnos.viewmodel.ReportarNovedadViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportarNovedadScreen(viewModel: ReportarNovedadViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Permite scroll general en pantallas pequeñas
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(bottom = 32.dp)
    ) {

        // 1. ENCABEZADO
        item {
            EncabezadoPantalla(
                titulo = "Reportar novedad",
                subtitulo = "Deja constancia de la diferencia entre el turno programado y el turno trabajado."
            )
        }

        // 2. FORMULARIO (Tarjeta principal)
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, CafeBorderLight),
                shape = RoundedCornerShape(2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    // A. Turno de referencia
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("TURNO DE REFERENCIA", color = CafeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(state.turnoReferencia, color = CafeTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Selector de fecha de referencia
                        Row(
                            modifier = Modifier
                                .clickable { viewModel.toggleDatePicker(true) }
                                .border(1.dp, CafeBorderLight, RoundedCornerShape(4.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(state.fechaReferencia, color = CafeDarkBrown, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.DateRange, contentDescription = "Cambiar fecha", tint = CafeDarkBrown, modifier = Modifier.size(16.dp))
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
                    HorizontalDivider(color = CafeBorderLight)
                    Spacer(modifier = Modifier.height(16.dp))

                    // B. Desplegable: Tipo de Novedad (Usando AppDropdown unificado)
                    AppDropdown(
                        label = "TIPO DE NOVEDAD",
                        valorActual = state.tipoNovedad,
                        opciones = state.opcionesNovedad,
                        onSeleccion = { viewModel.onTipoNovedadSeleccionado(it) },
                        colorFondo = CafeSurfaceWarm,
                        colorBorde = CafeBorderLight,
                        altura = 48.dp,
                        textoSize = 14.sp
                    )

                    if (state.requiereHoraReal) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("HORA REAL DEL EVENTO (HH:mm)", color = CafeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.horaReal,
                            onValueChange = { viewModel.onHoraRealCambio(it) },
                            placeholder = { Text("Ej. 13:30", color = CafeTextSecondary, fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(2.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = CafeSurfaceWarm,
                                focusedContainerColor = Color.White,
                                unfocusedBorderColor = CafeBorderLight,
                                focusedBorderColor = CafeDarkBrown
                            )
                        )

                        if (state.diferenciaCalculada.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, CafeAccentYellow.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Diferencia calculada: ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CafeDarkBrown)
                                    Text(
                                        text = state.diferenciaCalculada,
                                        fontSize = 12.sp,
                                        color = if (state.diferenciaCalculada.startsWith("-")) Color(0xFFC62828) else Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
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
                            unfocusedContainerColor = CafeSurfaceWarm,
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = CafeBorderLight,
                            focusedBorderColor = CafeDarkBrown
                        )
                    )

                    state.errorMensaje?.let { err ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFEF9A9A), RoundedCornerShape(2.dp)).padding(8.dp)
                        ) {
                            Text(err, color = Color(0xFFC62828), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    state.mensajeExito?.let { msg ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFA5D6A7), RoundedCornerShape(2.dp)).padding(8.dp)
                        ) {
                            Text(msg, color = Color(0xFF2E7D32), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }

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

        // Iteramos sobre las novedades utilizando el componente NovedadCard con key estable
        items(state.historial, key = { it.id }) { novedad ->
            NovedadCard(novedad = novedad)
        }
    }
}
