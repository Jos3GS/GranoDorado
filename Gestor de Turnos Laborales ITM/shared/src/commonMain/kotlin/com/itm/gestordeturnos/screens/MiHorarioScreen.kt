package com.itm.gestordeturnos.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.CalendarLocale
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.itm.gestordeturnos.CafeBorderLight
import com.itm.gestordeturnos.CafeDarkBrown
import com.itm.gestordeturnos.CafeSurfaceWarm
import com.itm.gestordeturnos.CafeTextSecondary
import com.itm.gestordeturnos.components.EncabezadoPantalla
import com.itm.gestordeturnos.components.TurnoCard
import com.itm.gestordeturnos.viewmodel.MiHorarioViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
private object MiHorarioUtcDatePickerFormatter : DatePickerFormatter {
    private val meses = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    override fun formatMonthYear(monthMillis: Long?, locale: CalendarLocale): String? {
        if (monthMillis == null) return null
        val instant = Instant.fromEpochMilliseconds(monthMillis)
        val ldt = instant.toLocalDateTime(TimeZone.UTC)
        val nombreMes = meses.getOrElse(ldt.monthNumber - 1) { "" }
        return "$nombreMes ${ldt.year}"
    }

    override fun formatDate(
        dateMillis: Long?,
        locale: CalendarLocale,
        forContentDescription: Boolean
    ): String? {
        if (dateMillis == null) return null
        val instant = Instant.fromEpochMilliseconds(dateMillis)
        val ldt = instant.toLocalDateTime(TimeZone.UTC)
        val dia = ldt.dayOfMonth.toString().padStart(2, '0')
        val mes = ldt.monthNumber.toString().padStart(2, '0')
        val anio = ldt.year
        val nombreMes = meses.getOrElse(ldt.monthNumber - 1) { "" }
        return if (forContentDescription) "$dia de $nombreMes de $anio" else "$dia/$mes/$anio"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiHorarioScreen(viewModel: MiHorarioViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var mostrarDatePickerSemana by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {

        // 1. ENCABEZADO CON SELECTOR DE SEMANA DINÁMICO
        val subtituloTexto = if (state.nombreEmpleado.isNotBlank()) {
            "${state.nombreEmpleado} · ${state.rangoSemana}"
        } else {
            state.rangoSemana
        }

        EncabezadoPantalla(
            titulo = "Mi horario",
            subtitulo = subtituloTexto,
            accion = {
                // Selector interactivo de semana (Flecha Izq, Botón Calendario con Rango, Flecha Der)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(6.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.cambiarSemana(-1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Semana anterior",
                            tint = CafeDarkBrown,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { mostrarDatePickerSemana = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Seleccionar fecha en calendario",
                            tint = CafeDarkBrown,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = state.rangoSemanaCorto,
                            color = CafeDarkBrown,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = { viewModel.cambiarSemana(1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Semana siguiente",
                            tint = CafeDarkBrown,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        )

        // 2. BARRA DE PUBLICACIÓN Y TOTAL DE HORAS
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chip de Estado de Publicación
            val (badgeBg, badgeBorder, badgeTextColor) = if (state.estaPublicado) {
                Triple(Color(0xFFD1FAE5), Color(0xFFA7F3D0), Color(0xFF065F46))
            } else {
                Triple(Color(0xFFFEF3C7), Color(0xFFFDE68A), Color(0xFFB45309))
            }

            Box(
                modifier = Modifier
                    .background(badgeBg, RoundedCornerShape(4.dp))
                    .border(1.dp, badgeBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = state.fechaPublicacion,
                    color = badgeTextColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            // Total de horas esta semana
            Row(
                modifier = Modifier
                    .background(CafeDarkBrown, RoundedCornerShape(4.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL ESTA SEMANA",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = state.totalHoras,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 3. LISTA DE DÍAS Y FOOTER
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // Renderizamos los 7 días de la semana
            items(state.turnos, key = { it.id }) { turno ->
                TurnoCard(turno = turno)
            }

            // Footer al final del scroll
            item {
                Spacer(modifier = Modifier.height(28.dp))

                // Navegación de semanas inferior
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { viewModel.cambiarSemana(-1) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "← SEMANA ANTERIOR",
                            color = CafeDarkBrown,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    TextButton(
                        onClick = { viewModel.cambiarSemana(1) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "SEMANA SIGUIENTE →",
                            color = CafeDarkBrown,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Caja de Solo Lectura informativa
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CafeSurfaceWarm)
                        .border(1.dp, CafeBorderLight, RoundedCornerShape(4.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = "SOLO LECTURA",
                            color = CafeTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val textoAviso = if (state.estaPublicado) {
                            "Cualquier cambio o solicitud de turno debe coordinarse directamente con tu supervisor o administrador de sede."
                        } else {
                            "Los turnos para esta semana se encuentran en borrador o pendientes de confirmación oficial."
                        }
                        Text(
                            text = textoAviso,
                            color = CafeTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    // 4. DIÁLOGO DATE PICKER (UTC)
    if (mostrarDatePickerSemana) {
        val initialMillis = remember(state.fechaInicioSemana) {
            state.fechaInicioSemana.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            initialDisplayedMonthMillis = initialMillis
        )

        DatePickerDialog(
            onDismissRequest = { mostrarDatePickerSemana = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val instant = Instant.fromEpochMilliseconds(millis)
                            val fechaSeleccionada = instant.toLocalDateTime(TimeZone.UTC).date
                            viewModel.seleccionarFechaSemana(fechaSeleccionada)
                        }
                        mostrarDatePickerSemana = false
                    }
                ) {
                    Text("Aceptar", color = CafeDarkBrown, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePickerSemana = false }) {
                    Text("Cancelar", color = Color(0xFF6B7280))
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                dateFormatter = remember { MiHorarioUtcDatePickerFormatter },
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = CafeDarkBrown,
                    selectedDayContentColor = Color.White,
                    todayDateBorderColor = CafeDarkBrown,
                    todayContentColor = CafeDarkBrown
                )
            )
        }
    }
}
