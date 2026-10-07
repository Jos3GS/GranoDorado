package com.itm.gestordeturnos.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itm.gestordeturnos.CafeDarkBrown
import com.itm.gestordeturnos.CafeTextPrimary
import com.itm.gestordeturnos.CafeTextSecondary
import com.itm.gestordeturnos.components.ModalSolicitarCambio
import com.itm.gestordeturnos.components.SolicitudCambioCard
import com.itm.gestordeturnos.viewmodel.CambiosTurnoViewModel

@Composable
fun CambiosTurnoScreen(viewModel: CambiosTurnoViewModel, isRolAdmin: Boolean) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {

        // 1. ENCABEZADO Y BOTÓN DINÁMICOS
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                Text("Cambios de turno", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = CafeTextPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isRolAdmin)
                        "Ningún turno cambia de responsable sin que el supervisor lo sepa y lo haya autorizado."
                    else
                        "Convierte un acuerdo verbal en un registro con solicitante, compañero, turnos y aprobación.",
                    fontSize = 16.sp,
                    color = CafeTextSecondary
                )
            }

            // El botón de crear solo lo ve el empleado
            if (!isRolAdmin) {
                Button(
                    // AQUÍ LLAMAMOS AL VIEWMODEL:
                    onClick = { viewModel.toggleModalSolicitud(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = CafeDarkBrown),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("+ Solicitar cambio", fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2. LISTA DE SOLICITUDES
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(bottom = 32.dp)
        ) {
            items(state.solicitudes) { solicitud ->
                // Le pasamos la bandera a cada tarjeta
                SolicitudCambioCard(solicitud = solicitud, isRolAdmin = isRolAdmin)
            }
        }
        // MOSTRAR EL MODAL
        if (state.mostrarModalSolicitud) {
            ModalSolicitarCambio(
                turnosPropios = state.turnosPropios,
                turnosCompaneros = state.turnosCompaneros,
                turnoSeleccionado = state.turnoPropioSeleccionado,
                onTurnoPropioSeleccionado = { viewModel.seleccionarTurnoPropio(it) },
                onDismiss = { viewModel.toggleModalSolicitud(false) }
            )
        }

    }
}