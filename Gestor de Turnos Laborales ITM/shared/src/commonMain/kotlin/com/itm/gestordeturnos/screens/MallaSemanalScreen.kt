package com.itm.gestordeturnos.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itm.gestordeturnos.components.EncabezadoPantalla
import com.itm.gestordeturnos.components.MallaEmployeeRow
import com.itm.gestordeturnos.components.MallaEstadoSucursalBadge
import com.itm.gestordeturnos.components.MallaHeaderRow
import com.itm.gestordeturnos.viewmodel.MallaSemanalViewModel

@Composable
fun MallaSemanalScreen(viewModel: MallaSemanalViewModel) {
    val state by viewModel.uiState.collectAsState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompact = maxWidth < 800.dp

        Column(modifier = Modifier.fillMaxSize()) {
            EncabezadoPantalla(
                titulo = "Malla semanal",
                subtitulo = state.rangoSemana,
                isCompact = isCompact,
                accion = {
                    MallaEstadoSucursalBadge(
                        sucursal = state.sucursal,
                        estadoMalla = state.estadoMalla
                    )
                }
            )

            val scrollState = rememberScrollState()
            Box(
                modifier = if (isCompact) Modifier.fillMaxSize().horizontalScroll(scrollState)
                           else Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = if (isCompact) Modifier.width(1000.dp).padding(bottom = 32.dp)
                               else Modifier.fillMaxSize().padding(bottom = 32.dp)
                ) {
                    item {
                        MallaHeaderRow(dias = state.diasSemana)
                    }

                    items(state.filas, key = { it.nombre }) { fila ->
                        MallaEmployeeRow(fila = fila)
                    }
                }
            }
        }
    }
}
