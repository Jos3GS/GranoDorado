package com.itm.gestordeturnos.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 32.dp),
    ){
        item{
            Column(
                modifier = Modifier.fillMaxWidth()
                    .padding(bottom = 24.dp)
            ){
                Text(
                    "Mi horario",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    state.rangoSemana,
                    fontSize = 14.sp,
                    color = CafeTextSecondary
                )
                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(
                        text = "HORARIO PUBLICADO EL ${state.fechaPublicacion}",
                        color = CafeTextSecondary,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                    )

                    Row(
                        modifier = Modifier
                            .background(CafeDarkBrown)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Text(
                            "TOTAL ESTA SEMANA",
                            color = CafeAccentYellow,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp,
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            state.totalHoras,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
        items(state.turnos){
            TurnoCard(turno = it)
        }
    }
}