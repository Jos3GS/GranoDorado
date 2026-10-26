package com.itm.gestordeturnos.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itm.gestordeturnos.CafeDarkBrown
import com.itm.gestordeturnos.CafeTextSecondary
import com.itm.gestordeturnos.components.EncabezadoPantalla
import com.itm.gestordeturnos.components.ModalSolicitarCambio
import com.itm.gestordeturnos.components.SolicitudCambioCard
import com.itm.gestordeturnos.viewmodel.CambiosTurnoViewModel

@Composable
fun CambiosTurnoScreen(viewModel: CambiosTurnoViewModel, isRolAdmin: Boolean) {
    val state by viewModel.uiState.collectAsState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompact = maxWidth < 600.dp

        Column(modifier = Modifier.fillMaxSize()) {

            // 1. ENCABEZADO Y BOTÓN DINÁMICOS
            EncabezadoPantalla(
                titulo = "Cambios de turno",
                subtitulo = if (isRolAdmin)
                    "Ningún turno cambia de responsable sin que el supervisor lo sepa y lo haya autorizado."
                else
                    "Convierte un acuerdo verbal en un registro con solicitante, compañero, turnos y aprobación.",
                isCompact = isCompact,
                accion = if (!isRolAdmin) {
                    {
                        Button(
                            onClick = { viewModel.toggleModalSolicitud(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = CafeDarkBrown),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("+ Solicitar cambio", fontWeight = FontWeight.Bold)
                        }
                    }
                } else null
            )

            // 2. LISTA DE SOLICITUDES
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(bottom = 32.dp)
            ) {
                if (state.solicitudes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isRolAdmin)
                                    "No hay solicitudes de cambio de turno para revisar."
                                else
                                    "No tienes solicitudes de cambio de turno registradas.",
                                color = CafeTextSecondary,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(state.solicitudes, key = { it.numeroSolicitud.ifBlank { it.id } }) { solicitud ->
                        SolicitudCambioCard(
                            solicitud = solicitud,
                            isRolAdmin = isRolAdmin,
                            onAprobar = { viewModel.aprobarSolicitud(it) },
                            onRechazar = { sol, mot -> viewModel.rechazarSolicitud(sol, mot) },
                            onAceptar = { viewModel.aceptarSolicitud(it) },
                            onCancelar = { viewModel.cancelarSolicitud(it) }
                        )
                    }
                }
            }

            // MOSTRAR EL MODAL
            if (state.mostrarModalSolicitud) {
                ModalSolicitarCambio(
                    turnosPropios = state.turnosPropios,
                    turnosCompaneros = state.turnosCompaneros,
                    turnoSeleccionado = state.turnoPropioSeleccionado,
                    turnoCompaneroSeleccionado = state.turnoCompaneroSeleccionado,
                    motivo = state.motivo,
                    onTurnoPropioSeleccionado = { viewModel.seleccionarTurnoPropio(it) },
                    onTurnoCompaneroSeleccionado = { viewModel.seleccionarTurnoCompanero(it) },
                    onMotivoCambiado = { viewModel.actualizarMotivo(it) },
                    onConfirmarSolicitud = { viewModel.enviarSolicitud() },
                    onDismiss = { viewModel.toggleModalSolicitud(false) }
                )
            }
        }
    }
}
