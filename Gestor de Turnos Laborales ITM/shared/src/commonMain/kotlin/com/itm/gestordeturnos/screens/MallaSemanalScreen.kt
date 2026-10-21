package com.itm.gestordeturnos.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import com.itm.gestordeturnos.CafeBackgroundLight
import com.itm.gestordeturnos.CafeDarkBrown
import com.itm.gestordeturnos.components.MallaEmployeeRow
import com.itm.gestordeturnos.components.MallaHeaderRow
import com.itm.gestordeturnos.components.MallaLeyenda
import com.itm.gestordeturnos.domain.model.FilaMalla
import com.itm.gestordeturnos.domain.model.TurnoCelda
import com.itm.gestordeturnos.viewmodel.MallaSemanalViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MallaSemanalScreen(viewModel: MallaSemanalViewModel) {
    val state by viewModel.uiState.collectAsState()
    var dropdownExpanded by remember { mutableStateOf(false) }
    var mostrarDatePickerSemana by remember { mutableStateOf(false) }

    // Estado de celda seleccionada (para futuro modal de asignación/edición)
    var celdaSeleccionada by remember { mutableStateOf<Triple<FilaMalla, Int, TurnoCelda>?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(CafeBackgroundLight)) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isCompact = maxWidth < 850.dp

            // Cálculo robusto de anchos de columna para evitar colapso de grid
            val colEmpleadoWidth = 180.dp
            val colTotalWidth = 100.dp
            val totalDivisoresWidth = 8.dp // 8 divisores verticales de 1.dp
            val anchoFijoColumnas = colEmpleadoWidth + colTotalWidth + totalDivisoresWidth
            val minColDiaWidth = 120.dp

            // Espacio disponible para las 7 columnas de días
            val anchoContenedor = if (maxWidth > 0.dp) maxWidth else 1150.dp
            val espacioDisponibleDias = anchoContenedor - anchoFijoColumnas
            val colDiaWidth = if (espacioDisponibleDias > minColDiaWidth * 7) {
                espacioDisponibleDias / 7
            } else {
                minColDiaWidth
            }
            val totalTableWidth = anchoFijoColumnas + (colDiaWidth * 7)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 32.dp)
            ) {
                // ==========================================
                // 1. CABECERA Y BARRA DE HERRAMIENTAS
                // ==========================================
                if (isCompact) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = state.titulo,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = CafeDarkBrown
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.subtitulo,
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Controles Superiores
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Dropdown Centro
                            Box {
                                OutlinedButton(
                                    onClick = { dropdownExpanded = true },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Color.White,
                                        contentColor = CafeDarkBrown
                                    ),
                                    border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = state.sucursal,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = CafeDarkBrown
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Seleccionar sucursal",
                                        tint = CafeDarkBrown,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = dropdownExpanded,
                                    onDismissRequest = { dropdownExpanded = false }
                                ) {
                                    state.opcionesSucursal.forEach { opcion ->
                                        DropdownMenuItem(
                                            text = { Text(opcion, color = CafeDarkBrown) },
                                            onClick = {
                                                viewModel.onSucursalCambio(opcion)
                                                dropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Botón Primario: Publicar semana
                            Button(
                                onClick = { viewModel.publicarSemana() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CafeDarkBrown,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = "Publicar semana",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 24.dp)) {
                            Text(
                                text = state.titulo,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = CafeDarkBrown
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.subtitulo,
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Dropdown Centro
                            Box {
                                OutlinedButton(
                                    onClick = { dropdownExpanded = true },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Color.White,
                                        contentColor = CafeDarkBrown
                                    ),
                                    border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = state.sucursal,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = CafeDarkBrown
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Seleccionar sucursal",
                                        tint = CafeDarkBrown,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = dropdownExpanded,
                                    onDismissRequest = { dropdownExpanded = false }
                                ) {
                                    state.opcionesSucursal.forEach { opcion ->
                                        DropdownMenuItem(
                                            text = { Text(opcion, color = CafeDarkBrown) },
                                            onClick = {
                                                viewModel.onSucursalCambio(opcion)
                                                dropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Botón Primario: Publicar semana
                            Button(
                                onClick = { viewModel.publicarSemana() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CafeDarkBrown,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = "Publicar semana",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ==========================================
                // 2. FILA DE CONTEXTO (Debajo del título) CON DATE PICKER
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Etiqueta tipo "Pill" con borde ocre y texto CENTRO
                    Box(
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = Color(0xFFD97706),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .background(Color.White, shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = state.sucursal.uppercase(),
                            color = Color(0xFFD97706),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Controles de navegación y DatePicker de semana
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(6.dp))
                    ) {
                        IconButton(
                            onClick = { viewModel.cambiarSemana(-1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Semana anterior",
                                tint = CafeDarkBrown,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Selector de fecha / rango de semana clickeable
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { mostrarDatePickerSemana = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Abrir calendario",
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = state.rangoSemana,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF374151)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.cambiarSemana(1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Semana siguiente",
                                tint = CafeDarkBrown,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Etiqueta de estado con fondo amarillo pálido y texto ocre en mayúsculas
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = Color(0xFFFDE68A),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = state.estadoMalla,
                            color = Color(0xFFB45309),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ==========================================
                // 3. TABLA PRINCIPAL (Grid de Turnos)
                // ==========================================
                val horizontalScrollState = rememberScrollState()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(horizontalScrollState)
                ) {
                    Card(
                        modifier = Modifier.width(totalTableWidth),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Cabecera de la tabla con anchos explícitos
                            MallaHeaderRow(
                                dias = state.diasSemana,
                                colEmpleadoWidth = colEmpleadoWidth,
                                colDiaWidth = colDiaWidth,
                                colTotalWidth = colTotalWidth
                            )
                            HorizontalDivider(thickness = 1.dp, color = Color(0xFFE5E7EB))

                            // Filas de empleados con celdas clickeables (excepto Vacaciones)
                            state.filas.forEachIndexed { index, fila ->
                                MallaEmployeeRow(
                                    fila = fila,
                                    colEmpleadoWidth = colEmpleadoWidth,
                                    colDiaWidth = colDiaWidth,
                                    colTotalWidth = colTotalWidth,
                                    onCeldaClick = { f, diaIdx, turno ->
                                        celdaSeleccionada = Triple(f, diaIdx, turno)
                                    }
                                )
                                if (index < state.filas.size - 1) {
                                    HorizontalDivider(thickness = 1.dp, color = Color(0xFFE5E7EB))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ==========================================
                // 4. LEYENDA (Parte inferior izquierda)
                // ==========================================
                MallaLeyenda()
            }
        }

        // ==========================================
        // 5. DIÁLOGO DATE PICKER PARA SELECCIONAR SEMANA
        // ==========================================
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
}
