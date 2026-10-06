package com.itm.gestordeturnos.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itm.gestordeturnos.CafeAccentYellow
import com.itm.gestordeturnos.CafeBorderColor
import com.itm.gestordeturnos.CafeDarkBrown
import com.itm.gestordeturnos.CafeTextPrimary
import com.itm.gestordeturnos.CafeTextSecondary
import com.itm.gestordeturnos.components.PrimarySolidButton
import com.itm.gestordeturnos.viewmodel.RolUsuario
import com.itm.gestordeturnos.viewmodel.UsuarioActual

@Composable
fun PanelScreen(usuario: UsuarioActual){
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ){
        val isCompact = maxWidth < 800.dp
        
        // Ajustamos el width para dispositivos móviles (50% menos el padding/spacing)
        val statCardWidth = if (isCompact) (maxWidth / 2) - 8.dp else 200.dp

        Column(
            modifier = Modifier.fillMaxWidth()
        ){
            GreetingHeader(
                nombre = usuario.nombre,
                rol = usuario.cargo,
                scope = if (usuario.rol == RolUsuario.ADMINISTRADOR_GENERAL)"VISTA DE LAS 3 SUCURSALES" else "CENTRO",
                isCompact = isCompact
            )
            Spacer(modifier = Modifier.height(32.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ){
                StatCard(
                    numero = if(usuario.rol == RolUsuario.ADMINISTRADOR_GENERAL) "11" else "7",
                    etiqueta = "EMPLEADOS ACTIVOS",
                    modifier = Modifier.width(statCardWidth)
                )
                if(usuario.rol == RolUsuario.ADMINISTRADOR_GENERAL){
                    StatCard(
                        numero = "3",
                        etiqueta = "TURNOS EN CATÁLOGO",
                        modifier = Modifier.width(statCardWidth)
                    )
                }
                StatCard(
                    numero = if (usuario.rol == RolUsuario.ADMINISTRADOR_GENERAL) "3" else "2",
                    etiqueta = "NOVEDADES PENDIENTES",
                    modifier = Modifier.width(statCardWidth)
                )
                StatCard(
                    numero = "1",
                    etiqueta = "CAMBIOS POR APROBAR",
                    modifier = Modifier.width(statCardWidth)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isCompact){
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ){
                    ActionBanner(modifier = Modifier.fillMaxWidth(), isCompact = isCompact)
                    DailyTask(modifier = Modifier.fillMaxWidth())
                }
            }else{
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ){
                    ActionBanner(modifier = Modifier.weight(0.6f), isCompact = isCompact)
                    DailyTask(modifier = Modifier.weight(0.4f))
                }
            }
        }
    }
}

@Composable
fun DailyTask(modifier: Modifier) {
    Card(
        modifier = modifier.height(220.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = MaterialTheme.shapes.extraSmall,
        border = BorderStroke(1.dp,CafeBorderColor)
    ){
        Column(
            modifier = Modifier.padding(24.dp).fillMaxSize()
        ){
            Text(
                "RONDA DEL DIA",
                color = CafeTextSecondary,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
            )
            Spacer(modifier = Modifier.height(16.dp))
            TaskItem("3 novedades esperan revisión")
            TaskItem("1 solicitudes de cambio por decidir")
            TaskItem("Publicar la semana antes del domingo")
        }
    }
}

@Composable
fun TaskItem(texto: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically
        ){
            Box(
                modifier = Modifier.size(6.dp)
                    .background(CafeAccentYellow, RoundedCornerShape(50))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                texto,
                fontSize = 12.sp,
                color = CafeTextPrimary
            )
        }
        Text("->", color = CafeTextSecondary)
    }
}

@Composable
fun ActionBanner(modifier: Modifier, isCompact: Boolean = false) {
    Box(
        modifier = modifier
            .height(240.dp)
            .background(Color(0xFFEBE6E0))
            .padding(24.dp)
    ){
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
        ){
            Column {
                Text(
                    "SEMANA EN CURSO 14 - 20 SEP 2026",
                    color = CafeTextSecondary,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "La malla esta en borrador",
                    fontSize = if (isCompact) 20.sp else 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Verifica que la franja 06:00 - 22:00 queda cubierta en cada sucursal antes de publicar el horario para los empleados.",
                    fontSize = 14.sp,
                    color = CafeTextSecondary
                )
            }

            PrimarySolidButton(
                text = "Abrir malla semanal ->",
                onClick = {},
                modifier = if (isCompact) Modifier.fillMaxWidth() else Modifier.width(250.dp)
            )
        }
    }
}

@Composable
fun StatCard(numero: String, etiqueta: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = MaterialTheme.shapes.extraSmall,
        border = BorderStroke(1.dp, CafeBorderColor)
    ){
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center
        ){
            Text(
                numero,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFA04E35)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                etiqueta,
                fontSize = 10.sp,
                color = CafeTextSecondary,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun GreetingHeader(nombre: String, rol: String, scope: String, isCompact: Boolean = false) {
    Column{
        Text(
            "BUENAS NOCHES",
            color = CafeTextSecondary,
            fontSize = 12.sp,
            letterSpacing = 1.sp
        )
        Text(
            nombre,
            fontSize = if (isCompact) 26.sp else 32.sp,
            fontWeight = FontWeight.Bold,
            color = CafeTextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ){
            Box(
                modifier = Modifier.background(CafeDarkBrown).padding(horizontal =  12.dp, vertical = 4.dp)
            ){
                Text(
                    rol.uppercase(),
                    color = Color.White,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                scope.uppercase(),
                color = CafeTextSecondary,
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )
        }
    }
}
