package com.itm.gestordeturnos.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itm.gestordeturnos.CafeDarkBrown
import com.itm.gestordeturnos.components.EncabezadoPantalla
import com.itm.gestordeturnos.components.FiltroDropdown
import com.itm.gestordeturnos.components.NovedadCard
import com.itm.gestordeturnos.viewmodel.NovedadesViewModel

@Composable
fun NovedadesScreen(viewModel: NovedadesViewModel) {
    val state by viewModel.uiState.collectAsState()
    val opcionesFiltro = listOf("Todas", "Reportada", "Revisada")

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompact = maxWidth < 800.dp

        Column(modifier = Modifier.fillMaxSize()) {
            EncabezadoPantalla(
                titulo = "Bandeja de novedades",
                subtitulo = "Lo que el equipo reporta llega aquí y queda constancia de que fue leído.",
                isCompact = isCompact,
                accion = {
                    FiltroDropdown(
                        opcionSeleccionada = state.filtroActual,
                        opciones = opcionesFiltro,
                        onOpcionSeleccionada = { viewModel.cambiarFiltro(it) }
                    )
                }
            )

            Box(
                modifier = Modifier
                    .background(Color(0xFFF3E7C9), RoundedCornerShape(2.dp))
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

            LazyColumn(modifier = Modifier.fillMaxSize().padding(bottom = 32.dp)) {
                items(state.novedadesFiltradas) { novedad ->
                    NovedadCard(novedad = novedad)
                }
            }
        }
    }
}
