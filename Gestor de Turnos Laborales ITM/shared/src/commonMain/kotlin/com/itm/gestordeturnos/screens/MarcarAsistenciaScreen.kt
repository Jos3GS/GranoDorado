package com.itm.gestordeturnos.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.itm.gestordeturnos.components.EncabezadoPantalla
import com.itm.gestordeturnos.components.SucursalChip
import com.itm.gestordeturnos.domain.model.EstadoAsistencia
import com.itm.gestordeturnos.viewmodel.MarcarAsistenciaViewModel

@Composable
fun MarcarAsistenciaScreen(viewModel: MarcarAsistenciaViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {

        // 1. ENCABEZADO
        EncabezadoPantalla(
            titulo = "Marcar asistencia",
            subtitulo = "Registra tu entrada y tu salida del turno de hoy.",
            horizontalAlignment = Alignment.CenterHorizontally
        )

        // 2. TARJETA CENTRAL ADAPTABLE
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val isCompact = maxWidth < 600.dp
            val paddingInterno = if (isCompact) 24.dp else 40.dp

            Card(
                modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, CafeBorderLight),
                shape = RoundedCornerShape(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(paddingInterno).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- INDICADOR DE ESTADO CON TOKENS SEMÁNTICOS ---
                    val (bgEstado, textEstado, textoEstado) = when {
                        state.esDescanso -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "DÍA DE DESCANSO")
                        state.estadoActual == EstadoAsistencia.SIN_INICIAR -> Triple(Color(0xFFF5F5F5), CafeTextSecondary, "SIN INICIAR")
                        state.estadoActual == EstadoAsistencia.EN_TURNO -> Triple(CafeStatusSuccessBg, CafeStatusSuccessText, "EN TURNO")
                        state.estadoActual == EstadoAsistencia.EN_DESCANSO -> Triple(CafeStatusInfoBg, CafeStatusInfoText, "EN DESCANSO")
                        state.estadoActual == EstadoAsistencia.FINALIZADO -> Triple(Color(0xFFF0F0F0), CafeTextSecondary, "FINALIZADO")
                        else -> Triple(Color(0xFFF5F5F5), CafeTextSecondary, "SIN INICIAR")
                    }

                    Box(
                        modifier = Modifier
                            .background(bgEstado, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(textoEstado, color = textEstado, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Datos del turno
                    Text(state.diaTexto, color = CafeTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(state.tipoTurno, color = CafeTextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(state.horario, color = CafeTextSecondary, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(14.dp))

                    SucursalChip(
                        sucursal = state.sucursal,
                        borderColor = CafeAccentYellow,
                        textColor = CafeAccentYellow
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- DETALLE DE MARCADORES REGISTRADOS ---
                    if (state.horaEntrada != "-" || state.inicioDescanso != "-" || state.horaSalida != "-") {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                            colors = CardDefaults.cardColors(containerColor = CafeSurfaceWarm),
                            border = BorderStroke(1.dp, CafeBorderLight),
                            shape = RoundedCornerShape(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                                Text(
                                    "REGISTROS DE HOY",
                                    color = CafeTextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Entrada", fontSize = 11.sp, color = CafeTextSecondary)
                                        Text(state.horaEntrada, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CafeTextPrimary)
                                    }
                                    if (state.inicioDescanso != "-") {
                                        Column {
                                            Text("Descanso", fontSize = 11.sp, color = CafeTextSecondary)
                                            val descTexto = if (state.finDescanso != "-") "${state.inicioDescanso} - ${state.finDescanso}" else "${state.inicioDescanso} (En curso)"
                                            Text(descTexto, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CafeTextPrimary)
                                        }
                                    }
                                    Column {
                                        Text("Salida", fontSize = 11.sp, color = CafeTextSecondary)
                                        Text(state.horaSalida, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CafeTextPrimary)
                                    }
                                }
                            }
                        }
                    }

                    // --- MENSAJE DE AVISO / CONFIRMACIÓN ---
                    state.mensajeAviso?.let { aviso ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CafeSurfaceWarm, RoundedCornerShape(2.dp))
                                .border(BorderStroke(1.dp, CafeBorderLight), RoundedCornerShape(2.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = aviso,
                                color = CafeTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // --- BOTONES DINÁMICOS SEGÚN EL ESTADO ---
                    when {
                        state.esDescanso -> {
                            Button(
                                onClick = { },
                                enabled = false,
                                colors = ButtonDefaults.buttonColors(disabledContainerColor = CafeBorderLight),
                                shape = RoundedCornerShape(2.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("Día de descanso programado", color = CafeTextSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        !state.tieneTurnoAsignado -> {
                            Button(
                                onClick = { },
                                enabled = false,
                                colors = ButtonDefaults.buttonColors(disabledContainerColor = CafeBorderLight),
                                shape = RoundedCornerShape(2.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("Sin turno programado para hoy", color = CafeTextSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        state.estadoActual == EstadoAsistencia.SIN_INICIAR -> {
                            Button(
                                onClick = { viewModel.marcarEntrada() },
                                colors = ButtonDefaults.buttonColors(containerColor = CafeDarkBrown),
                                shape = RoundedCornerShape(2.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("Marcar entrada", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        state.estadoActual == EstadoAsistencia.EN_TURNO -> {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                OutlinedButton(
                                    onClick = { viewModel.iniciarDescanso() },
                                    border = BorderStroke(1.dp, CafeBorderLight),
                                    shape = RoundedCornerShape(2.dp),
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {
                                    Text("Ir a descanso", color = CafeTextPrimary, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { viewModel.marcarSalida() },
                                    colors = ButtonDefaults.buttonColors(containerColor = CafeDarkBrown),
                                    shape = RoundedCornerShape(2.dp),
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {
                                    Text("Marcar salida", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        state.estadoActual == EstadoAsistencia.EN_DESCANSO -> {
                            Button(
                                onClick = { viewModel.finalizarDescanso() },
                                colors = ButtonDefaults.buttonColors(containerColor = CafeDarkBrown),
                                shape = RoundedCornerShape(2.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("Finalizar descanso", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        state.estadoActual == EstadoAsistencia.FINALIZADO -> {
                            Button(
                                onClick = { },
                                enabled = false,
                                colors = ButtonDefaults.buttonColors(disabledContainerColor = CafeBorderLight),
                                shape = RoundedCornerShape(2.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("Jornada completada", color = CafeTextSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
