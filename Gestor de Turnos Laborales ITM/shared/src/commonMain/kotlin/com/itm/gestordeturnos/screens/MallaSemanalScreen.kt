package com.itm.gestordeturnos.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itm.gestordeturnos.CafeTextPrimary
import com.itm.gestordeturnos.CafeTextSecondary
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
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                if (isCompact) {
                    Text(
                        "Malla semanal",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = CafeTextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    MallaEstadoSucursalBadge(
                        sucursal = state.sucursal,
                        estadoMalla = state.estadoMalla
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Malla semanal",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = CafeTextPrimary
                        )
                        MallaEstadoSucursalBadge(
                            sucursal = state.sucursal,
                            estadoMalla = state.estadoMalla
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    state.rangoSemana,
                    fontSize = 14.sp,
                    color = CafeTextSecondary
                )
            }

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

                    items(state.filas) { fila ->
                        MallaEmployeeRow(fila = fila)
                    }
                }
            }
        }
    }
}
