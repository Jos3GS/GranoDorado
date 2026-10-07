package com.itm.gestordeturnos.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itm.gestordeturnos.CafeAccentYellow
import com.itm.gestordeturnos.CafeDarkBrown
import com.itm.gestordeturnos.CafeTextPrimary
import com.itm.gestordeturnos.CafeTextSecondary
import com.itm.gestordeturnos.components.TurnoCard
import com.itm.gestordeturnos.viewmodel.MiHorarioViewModel

@Composable
fun MiHorarioScreen(viewModel: MiHorarioViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {

        // 1. ENCABEZADO
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            Text("ÉPICA 3 · HU-09", color = CafeTextSecondary, fontSize = 10.sp, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(4.dp))

            Text("Mi horario", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = CafeTextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(state.rangoSemana, fontSize = 16.sp, color = CafeTextSecondary)
        }

        // 2. BARRA DE PUBLICACIÓN Y TOTAL DE HORAS
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                state.fechaPublicacion,
                color = CafeTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            // Caja negra con total de horas
            Row(
                modifier = Modifier
                    .background(CafeDarkBrown, RoundedCornerShape(2.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("TOTAL ESTA SEMANA", color = CafeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(state.totalHoras, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 3. LISTA DE DÍAS Y FOOTER
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // Renderizamos los días de la semana
            items(state.turnos) { turno ->
                TurnoCard(turno = turno)
            }

            // Agregamos el Footer al final del scroll
            item {
                Spacer(modifier = Modifier.height(32.dp))

                // Navegación de semanas
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("← SEMANA ANTERIOR", color = CafeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text("SEMANA SIGUIENTE →", color = CafeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }

                // Caja de Solo Lectura
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAF9F6)) // Crema claro
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(2.dp))
                        .padding(24.dp)
                ) {
                    Column {
                        Text("SOLO LECTURA", color = CafeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Cualquier cambio pasa por una solicitud de cambio de turno o por tu administrador. La semana del 21 al 27 de septiembre todavía no está publicada.",
                            color = CafeTextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp)) // Espacio final
            }
        }
    }
}