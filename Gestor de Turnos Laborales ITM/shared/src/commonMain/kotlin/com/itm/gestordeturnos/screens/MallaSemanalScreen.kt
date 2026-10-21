package com.itm.gestordeturnos.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.itm.gestordeturnos.CafeBackgroundLight
import com.itm.gestordeturnos.CafeDarkBrown
import com.itm.gestordeturnos.components.MallaEmployeeRow
import com.itm.gestordeturnos.components.MallaHeaderRow
import com.itm.gestordeturnos.components.MallaLeyenda
import com.itm.gestordeturnos.viewmodel.MallaSemanalViewModel

@Composable
fun MallaSemanalScreen(viewModel: MallaSemanalViewModel) {
    val state by viewModel.uiState.collectAsState()
    var dropdownExpanded by remember { mutableStateOf(false) }
    var mostrarAyuda by remember { mutableStateOf(false) }

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
                    .padding(bottom = 60.dp)
            ) {
                // ==========================================
                // 1. CABECERA Y BARRA DE HERRAMIENTAS
                // ==========================================
                if (isCompact) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Supratítulo gris de la épica
                        Text(
                            text = "ÉPICA 2 · MALLA SEMANAL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B7280),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
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
                            // Supratítulo gris de la épica
                            Text(
                                text = "ÉPICA 2 · MALLA SEMANAL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B7280),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
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
                // 2. FILA DE CONTEXTO (Debajo del título)
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

                    // Rango de fechas en texto estándar: 14 – 20 sep 2026
                    Text(
                        text = state.rangoSemana,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )

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

                            // Filas de empleados con anchos explícitos
                            state.filas.forEachIndexed { index, fila ->
                                MallaEmployeeRow(
                                    fila = fila,
                                    colEmpleadoWidth = colEmpleadoWidth,
                                    colDiaWidth = colDiaWidth,
                                    colTotalWidth = colTotalWidth
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
        // 5. BOTÓN FLOTANTE DE AYUDA '?' (Inferior Derecha)
        // ==========================================
        FloatingActionButton(
            onClick = { mostrarAyuda = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(44.dp),
            containerColor = CafeDarkBrown,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Text(
                text = "?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Diálogo de Ayuda
        if (mostrarAyuda) {
            AlertDialog(
                onDismissRequest = { mostrarAyuda = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Ayuda",
                            tint = CafeDarkBrown,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Guía: Malla Semanal",
                            fontWeight = FontWeight.Bold,
                            color = CafeDarkBrown,
                            fontSize = 18.sp
                        )
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "• Cobertura de jornada: Verifica que la franja de 06:00 a 22:00 quede totalmente cubierta antes de publicar la semana.",
                            fontSize = 13.sp,
                            color = Color(0xFF374151)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Alertas: Los días con huecos de personal se resaltan en rojo con la etiqueta 'SIN CUBRIR'.",
                            fontSize = 13.sp,
                            color = Color(0xFF374151)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Publicación: Una vez verificados los turnos, presiona 'Publicar semana' para hacer visible la malla a los empleados.",
                            fontSize = 13.sp,
                            color = Color(0xFF374151)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { mostrarAyuda = false }) {
                        Text("Entendido", color = CafeDarkBrown, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}
