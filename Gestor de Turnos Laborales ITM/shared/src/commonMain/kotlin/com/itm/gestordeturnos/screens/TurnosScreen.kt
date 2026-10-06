package com.itm.gestordeturnos.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.itm.gestordeturnos.viewmodel.TurnoTemplate
import com.itm.gestordeturnos.viewmodel.TurnosViewModel

val ColorApertura = Color(0xFFC48E38)
val ColorIntermedio = Color(0xFF6B7E4B)
val ColorCierre = Color(0xFF4C7B8B)

@Composable
fun TurnosScreen(viewModel: TurnosViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ){
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ){
            Column(
                modifier = Modifier.weight(1f)
            ){
                Text(
                    "Catálogo de turnos",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeTextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Franjas reutilizables que convierten la programación en una selección, no una redacción. Operación 06:00 - 22:00.",
                    fontSize = 14.sp,
                    color = CafeTextSecondary
                )
            }

            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = CafeDarkBrown),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(start = 16.dp)
            ){
                Text(
                    "+ Crear turno",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        BoxWithConstraints {
            val isDesktop = maxWidth > 800.dp

            if (isDesktop){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ){
                    Row(
                        modifier = Modifier.weight(2f)
                    ){
                        TablaTurnos(turnos = state.turnos, modifier = Modifier.fillMaxWidth())
                    }

                    Row(
                        modifier = Modifier.weight(1f)
                    ){
                        CoberturaCard()
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 32.dp)
                ){
                    CoberturaCard()
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ){
                        TablaTurnos(turnos = state.turnos, modifier = Modifier.width(800.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CoberturaCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.fillMaxWidth()
    ){
        Column(
            modifier = Modifier.padding(24.dp)
        ){
            Text(
                "COBERTURA DE LA FRANJA",
                color = CafeTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    "06:00",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeTextPrimary
                )
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.padding(horizontal = 8.dp)
                        .size(16.dp)
                )
                Text(
                    "22:00",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
            ){
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(ColorApertura))
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(ColorIntermedio))
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(ColorCierre))
            }

            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("06", "10", "14", "18", "22").forEach { hora ->
                    Text(hora, fontSize = 10.sp, color = CafeTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LeyendaTurno(ColorApertura, "Apertura", "06:00-14:00")
            Spacer(modifier = Modifier.height(8.dp))
            LeyendaTurno(ColorIntermedio, "Intermedio", "10:00-18:00")
            Spacer(modifier = Modifier.height(8.dp))
            LeyendaTurno(ColorCierre, "Cierre", "14:00-22:00")

            Spacer(modifier = Modifier.height(24.dp))

            Text("El catálogo debe cubrir el 100 % de la franja sin huecos ni solapamientos no intencionales.", color = CafeTextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
fun LeyendaTurno(color: Color, nombre: String, horario: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically
        ){
            Box(modifier = Modifier.size(12.dp).background(color, RoundedCornerShape(2.dp)))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                nombre,
                fontSize = 12.sp,
                color = CafeTextPrimary
            )
        }
        Text(
            horario,
            fontSize = 10.sp,
            color = CafeTextSecondary,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun TablaTurnos(turnos: List<TurnoTemplate>, modifier: Modifier) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ){
        Column {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                CabeceraTabla("TURNO", modifier = Modifier.weight(1.5f))
                CabeceraTabla("HORARIO", modifier = Modifier.weight(1f))
                CabeceraTabla("DESCANSOS", modifier = Modifier.weight(1f))
                CabeceraTabla("EFECTIVA", modifier = Modifier.weight(1f))
                CabeceraTabla("ESTADO", modifier = Modifier.weight(1.2f))
                CabeceraTabla("ASIGNACIONES", modifier = Modifier.weight(1.5f))
            }
            HorizontalDivider(color = Color(0xFFE0E0E0))

            turnos.forEach { turno ->
                FilaTurno(turno)
                HorizontalDivider(color = Color(0xFFE0E0E0))
            }
        }
    }
}

@Composable
fun FilaTurno(turno: TurnoTemplate) {
    Row(
        modifier = Modifier.fillMaxWidth().padding( 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(
            turno.nombre,
            modifier = Modifier.weight(1.5f),
            color = CafeTextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            turno.horario,
            modifier = Modifier.weight(1f),
            color = CafeTextPrimary,
            fontSize = 12.sp,
        )
        Text(
            turno.descansos,
            modifier = Modifier.weight(1f),
            color = CafeTextSecondary,
            fontSize = 12.sp
        )
        Text(
            turno.horasEfectivas,
            modifier = Modifier.weight(1f),
            color = CafeAccentYellow,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        Box(
            modifier = Modifier.weight(1.2f)
        ){
            val isActivo = turno.estado == "ACTIVO"
            val bgColor = if (isActivo) Color(0xFFE6F0E6) else Color(0xFFFBEBEB)
            val textColor = if (isActivo) Color(0xFF4C7B4C) else Color(0xFFB04D4D)
            val dotColor = if (isActivo) Color(0xFF4C7B4C) else Color(0xFFD32F2F)

            Row(
                modifier = Modifier.background(color = bgColor, shape = RoundedCornerShape(2.dp)).padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Box(modifier = Modifier.size(6.dp).background(dotColor, CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    turno.estado,
                    color = textColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Column(modifier = Modifier.weight(1.5f)) {
            Text(
                "${turno.asignaciones} solo",
                color = CafeTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "inactivar",
                color = CafeTextSecondary,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
fun CabeceraTabla(texto: String, modifier: Modifier) {
    Text(
        text = texto,
        modifier = modifier,
        color = CafeTextSecondary,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
}