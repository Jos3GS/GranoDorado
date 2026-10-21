package com.itm.gestordeturnos.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.itm.gestordeturnos.CafeBorderColor
import com.itm.gestordeturnos.CafeDarkBrown
import com.itm.gestordeturnos.CafeStatusErrorBg
import com.itm.gestordeturnos.CafeStatusErrorText
import com.itm.gestordeturnos.CafeStatusSuccessBg
import com.itm.gestordeturnos.CafeStatusSuccessText
import com.itm.gestordeturnos.CafeTextPrimary
import com.itm.gestordeturnos.CafeTextSecondary
import com.itm.gestordeturnos.ColorApertura
import com.itm.gestordeturnos.ColorCierre
import com.itm.gestordeturnos.ColorIntermedio
import com.itm.gestordeturnos.components.EncabezadoPantalla
import com.itm.gestordeturnos.components.ModalCrearTurno
import com.itm.gestordeturnos.domain.model.TurnoTemplate
import com.itm.gestordeturnos.viewmodel.TurnosViewModel

private val HORAS_COBERTURA = listOf("06", "10", "14", "18", "22")

@Composable
fun TurnosScreen(viewModel: TurnosViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ){
        EncabezadoPantalla(
            titulo = "Catálogo de turnos",
            subtitulo = "Franjas reutilizables que convierten la programación en una selección, no una redacción. Operación 06:00 - 22:00.",
            accion = {
                Button(
                    onClick = { viewModel.toggleModalCrearTurno(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = CafeDarkBrown),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        "+ Crear turno",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )

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
                        TablaTurnos(
                            turnos = state.turnos,
                            onEditarTurno = { viewModel.abrirEditarTurno(it) },
                            modifier = Modifier.fillMaxWidth()
                        )
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
                        TablaTurnos(
                            turnos = state.turnos,
                            onEditarTurno = { viewModel.abrirEditarTurno(it) },
                            modifier = Modifier.width(850.dp)
                        )
                    }
                }
            }
        }
    }

    // LÓGICA DEL MODAL (Crear o Editar)
    if (state.mostrarModalCrearTurno) {
        ModalCrearTurno(
            nombre = state.nombreTurnoNuevo,
            horaInicio = state.horaInicioNuevo,
            horaFin = state.horaFinNuevo,
            descansos = state.descansosNuevo,
            mensajeError = state.mensajeErrorNuevo,
            horasEfectivas = state.horasEfectivasCalculadas,
            esEdicion = state.esEdicion,
            estado = state.estadoTurnoNuevo,
            onNombreChange = { viewModel.onNombreTurnoCambio(it) },
            onHoraInicioChange = { viewModel.onHoraInicioCambio(it) },
            onHoraFinChange = { viewModel.onHoraFinCambio(it) },
            onEstadoChange = { viewModel.onEstadoTurnoCambio(it) },
            onAgregarDescanso = { viewModel.agregarDescanso() },
            onQuitarDescanso = { viewModel.quitarDescanso(it) },
            onDescansoInicioChange = { id, hora -> viewModel.onDescansoInicioCambio(id, hora) },
            onDescansoFinChange = { id, hora -> viewModel.onDescansoFinCambio(id, hora) },
            onGuardar = { viewModel.guardarTurno() },
            onDismiss = { viewModel.toggleModalCrearTurno(false) }
        )
    }
}

@Composable
fun CoberturaCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        border = BorderStroke(1.dp, CafeBorderColor),
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
                HORAS_COBERTURA.forEach { hora ->
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
fun TablaTurnos(
    turnos: List<TurnoTemplate>,
    onEditarTurno: (TurnoTemplate) -> Unit,
    modifier: Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        border = BorderStroke(1.dp, CafeBorderColor),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ){
        Column {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                CabeceraTabla("TURNO", modifier = Modifier.weight(1.4f))
                CabeceraTabla("HORARIO", modifier = Modifier.weight(1f))
                CabeceraTabla("DESCANSOS", modifier = Modifier.weight(1f))
                CabeceraTabla("EFECTIVA", modifier = Modifier.weight(1f))
                CabeceraTabla("ESTADO", modifier = Modifier.weight(1.1f))
                CabeceraTabla("EMPLEADOS", modifier = Modifier.weight(1.2f))
                CabeceraTabla("ACCIONES", modifier = Modifier.weight(1.1f))
            }
            HorizontalDivider(color = CafeBorderColor)

            turnos.forEach { turno ->
                FilaTurno(
                    turno = turno,
                    onEditar = { onEditarTurno(turno) }
                )
                HorizontalDivider(color = CafeBorderColor)
            }
        }
    }
}

@Composable
fun FilaTurno(
    turno: TurnoTemplate,
    onEditar: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(
            turno.nombre,
            modifier = Modifier.weight(1.4f),
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
            modifier = Modifier.weight(1.1f)
        ){
            val isActivo = turno.estado == "ACTIVO"
            val bgColor = if (isActivo) CafeStatusSuccessBg else CafeStatusErrorBg
            val textColor = if (isActivo) CafeStatusSuccessText else CafeStatusErrorText
            val dotColor = if (isActivo) CafeStatusSuccessText else CafeStatusErrorText

            Row(
                modifier = Modifier
                    .background(color = bgColor, shape = RoundedCornerShape(2.dp))
                    .border(BorderStroke(0.5.dp, textColor.copy(alpha = 0.3f)), RoundedCornerShape(2.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
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

        Text(
            "${turno.asignaciones} ${if (turno.asignaciones == 1) "empleado" else "empleados"}",
            modifier = Modifier.weight(1.2f),
            color = CafeTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.weight(1.1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .border(1.dp, CafeBorderColor, RoundedCornerShape(4.dp))
                    .background(Color.White, RoundedCornerShape(4.dp))
                    .clickable { onEditar() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    "Editar",
                    color = CafeDarkBrown,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
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
