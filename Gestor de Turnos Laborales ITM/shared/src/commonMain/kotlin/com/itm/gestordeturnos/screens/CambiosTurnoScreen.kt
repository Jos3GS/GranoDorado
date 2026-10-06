package com.itm.gestordeturnos.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itm.gestordeturnos.CafeTextPrimary
import com.itm.gestordeturnos.CafeTextSecondary
import com.itm.gestordeturnos.components.SolicitudCambioCard
import com.itm.gestordeturnos.viewmodel.CambiosTurnoViewModel

@Composable
fun CambiosTurnoScreen(viewModel: CambiosTurnoViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {

        // 1. ENCABEZADO (Idéntico al Figma)
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {

            Text("Aprobar cambios de turno", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = CafeTextPrimary)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Ningún turno cambia de responsable sin que el supervisor lo sepa y lo haya autorizado.",
                fontSize = 16.sp,
                color = CafeTextSecondary
            )
        }

        // 2. LISTA DE SOLICITUDES
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(bottom = 32.dp)
        ) {
            items(state.solicitudes) { solicitud ->
                SolicitudCambioCard(solicitud = solicitud)
            }
        }
    }
}