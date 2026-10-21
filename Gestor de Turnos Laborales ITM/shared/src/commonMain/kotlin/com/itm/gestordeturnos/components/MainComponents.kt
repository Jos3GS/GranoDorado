package com.itm.gestordeturnos.components

import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import com.itm.gestordeturnos.domain.model.DiaColumnaMalla
import com.itm.gestordeturnos.domain.model.TipoCeldaMalla

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.itm.gestordeturnos.CafeAccentYellow
import com.itm.gestordeturnos.CafeBackgroundLight
import com.itm.gestordeturnos.CafeBorderColor
import com.itm.gestordeturnos.CafeBorderLight
import com.itm.gestordeturnos.CafeDarkBrown
import com.itm.gestordeturnos.CafeStatusErrorBg
import com.itm.gestordeturnos.CafeStatusErrorText
import com.itm.gestordeturnos.CafeStatusInfoBg
import com.itm.gestordeturnos.CafeStatusInfoText
import com.itm.gestordeturnos.CafeStatusSuccessBg
import com.itm.gestordeturnos.CafeStatusSuccessText
import com.itm.gestordeturnos.CafeStatusWarningBg
import com.itm.gestordeturnos.CafeSurfaceWarm
import com.itm.gestordeturnos.CafeTextPrimary
import com.itm.gestordeturnos.CafeTextSecondary
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.FilaMalla
import com.itm.gestordeturnos.domain.model.Novedad
import com.itm.gestordeturnos.domain.model.RangoDescanso
import com.itm.gestordeturnos.domain.model.SolicitudCambio
import com.itm.gestordeturnos.domain.model.TurnoAsignado
import com.itm.gestordeturnos.domain.model.TurnoCelda
import com.itm.gestordeturnos.domain.model.TurnoCompaneroSeleccionable
import com.itm.gestordeturnos.domain.model.TurnoSeleccionable

@Composable
fun DrawerMenuItem(
    titulo: String,
    isSelected: Boolean,
    onClick: () -> Unit
){
    val backgroundColor = if (isSelected) CafeAccentYellow else Color.Transparent
    val textColor = if (isSelected) CafeDarkBrown else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(backgroundColor)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(
            text = titulo,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun UserProfileInfo(
    nombre: String,
    iniciales: String,
    cargo: String,
    onLogoutClick: () -> Unit
){
    Column(
        modifier = Modifier.padding(24.dp)
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically
        ){
            Box(
                modifier = Modifier.size(40.dp).background(CafeBackgroundLight),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = iniciales,
                    fontWeight = FontWeight.Bold,
                    color = CafeDarkBrown
                )
            }
            Spacer(modifier = Modifier.width(12.dp))

            Column{
                Text(
                    text = nombre,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = cargo.uppercase(),
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Cerrar sesión",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            modifier = Modifier.clickable { onLogoutClick() }
        )
    }
}

@Composable
fun TurnoCard(turno: TurnoAsignado) {
    val colorBorde = if (turno.esDescanso) CafeAccentYellow else CafeBorderColor

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, colorBorde),
        shape = RoundedCornerShape(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Columna Izquierda: LUN 14
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(40.dp)
            ) {
                Text(
                    text = turno.diaSemana,
                    color = CafeTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = turno.numeroDia,
                    color = CafeTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 2. Línea Divisoria Vertical
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(CafeBorderColor)
            )
            Spacer(modifier = Modifier.width(16.dp))

            // 3. Columna Central: Apertura / Cierre / Descanso
            Column(modifier = Modifier.weight(1f)) {
                when {
                    turno.esDescanso -> {
                        Box(
                            modifier = Modifier
                                .background(CafeStatusInfoBg, RoundedCornerShape(2.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "DESCANSO",
                                color = CafeStatusInfoText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                    turno.sinAsignar -> {
                        Text(
                            text = "Sin turno asignado",
                            color = CafeTextSecondary,
                            fontSize = 14.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                    else -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = turno.tipoTurno ?: "",
                                color = CafeTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (turno.etiquetaModificado != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(CafeStatusWarningBg, RoundedCornerShape(2.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = turno.etiquetaModificado,
                                        color = CafeDarkBrown,
                                        fontSize = 10.sp,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = turno.horario ?: "",
                            color = CafeTextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // 4. Columna Derecha: Sucursal y Total Horas
            if (!turno.esDescanso && !turno.sinAsignar) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SucursalChip(
                        sucursal = turno.sucursal ?: "",
                        borderColor = CafeAccentYellow,
                        textColor = CafeAccentYellow
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = turno.duracion ?: "",
                        color = CafeTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun MallaHeaderRow(
    dias: List<DiaColumnaMalla>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Columna 1: EMPLEADO (Alineado a la izquierda, texto gris)
        Text(
            text = "EMPLEADO",
            color = Color(0xFF6B7280),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.width(170.dp)
        )

        // Columnas 2-8 (DÃ­as de la semana)
        dias.forEach { dia ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // DÃ­a abreviado (ej. LUN)
                Text(
                    text = dia.diaSemana.uppercase(),
                    color = Color(0xFF6B7280),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                // NÃºmero del dÃ­a (ej. 14 en negrita oscura)
                Text(
                    text = dia.numeroDia,
                    color = CafeDarkBrown,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Indicador de cobertura
                if (dia.cubierto) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF16A34A), shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "CUBIERTO",
                            color = Color(0xFF6B7280),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Canvas(modifier = Modifier.size(7.dp)) {
                            val path = Path().apply {
                                moveTo(size.width / 2f, 0f)
                                lineTo(size.width, size.height)
                                lineTo(0f, size.height)
                                close()
                            }
                            drawPath(path, color = Color(0xFFDC2626))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SIN CUBRIR",
                            color = Color(0xFFDC2626),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // Columna 9: TOTAL (Alineado al centro, texto gris)
        Text(
            text = "TOTAL",
            color = Color(0xFF6B7280),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(90.dp)
        )
    }
}

// Sobrecarga de compatibilidad para listas de String

@Composable
fun MallaHeaderRow(
    dias: List<DiaColumnaMalla>,
    colEmpleadoWidth: Dp = 180.dp,
    colDiaWidth: Dp = 120.dp,
    colTotalWidth: Dp = 100.dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(Color.White)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Columna 1: EMPLEADO (Alineado a la izquierda, texto gris)
        Box(
            modifier = Modifier
                .width(colEmpleadoWidth)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "EMPLEADO",
                color = Color(0xFF6B7280),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        // Divisor vertical
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(Color(0xFFE5E7EB))
        )

        // Columnas 2-8 (Días de la semana)
        dias.forEachIndexed { index, dia ->
            Box(
                modifier = Modifier
                    .width(colDiaWidth)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Día abreviado (ej. LUN)
                    Text(
                        text = dia.diaSemana.uppercase(),
                        color = Color(0xFF6B7280),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    // Número del día (ej. 14 en negrita oscura)
                    Text(
                        text = dia.numeroDia,
                        color = CafeDarkBrown,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Indicador de cobertura
                    if (dia.cubierto) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFF16A34A), shape = CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "CUBIERTO",
                                color = Color(0xFF6B7280),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Canvas(modifier = Modifier.size(7.dp)) {
                                val path = Path().apply {
                                    moveTo(size.width / 2f, 0f)
                                    lineTo(size.width, size.height)
                                    lineTo(0f, size.height)
                                    close()
                                }
                                drawPath(path, color = Color(0xFFDC2626))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SIN CUBRIR",
                                color = Color(0xFFDC2626),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            // Divisor vertical entre días
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFE5E7EB))
            )
        }

        // Columna 9: TOTAL (Alineado al centro, texto gris)
        Box(
            modifier = Modifier
                .width(colTotalWidth)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "TOTAL",
                color = Color(0xFF6B7280),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Sobrecarga de compatibilidad para listas de String
@Composable
fun MallaHeaderRow(
    diasNombres: List<String>,
    modifier: Modifier = Modifier
) {
    val dias = diasNombres.mapIndexed { idx, d ->
        DiaColumnaMalla(diaSemana = d, numeroDia = "${14 + idx}", cubierto = d != "SÁB" && d != "SAB")
    }
    MallaHeaderRow(dias = dias, modifier = modifier)
}


@Composable
fun MallaEmployeeRow(
    fila: FilaMalla,
    colEmpleadoWidth: Dp = 180.dp,
    colDiaWidth: Dp = 120.dp,
    colTotalWidth: Dp = 100.dp,
    onCeldaClick: ((fila: FilaMalla, diaIndex: Int, turno: TurnoCelda) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(Color.White)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Columna 1: Empleado (Nombre en negrita oscura y rol en mayúsculas y gris)
        Box(
            modifier = Modifier
                .width(colEmpleadoWidth)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = fila.nombre,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeDarkBrown,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = fila.cargo.uppercase(),
                    fontSize = 10.sp,
                    color = Color(0xFF6B7280),
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Divisor vertical
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(Color(0xFFE5E7EB))
        )

        // Columnas 2-8: Celdas de turnos diarios
        fila.turnos.forEachIndexed { index, turno ->
            Box(
                modifier = Modifier
                    .width(colDiaWidth)
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                MallaCeldaTurno(
                    turno = turno,
                    onClick = if (turno.tipoCelda != TipoCeldaMalla.VACACIONES) {
                        { onCeldaClick?.invoke(fila, index, turno) }
                    } else null
                )
            }

            // Divisor vertical entre días
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFE5E7EB))
            )
        }

        // Columna 9: Totales (Suma de horas en color ocre/naranja)
        Box(
            modifier = Modifier
                .width(colTotalWidth)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = fila.totalHoras,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD97706),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MallaCeldaTurno(
    turno: TurnoCelda,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val cellShape = RoundedCornerShape(6.dp)
    val esClickeable = turno.tipoCelda != TipoCeldaMalla.VACACIONES && onClick != null

    when (turno.tipoCelda) {
        TipoCeldaMalla.TURNO -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(CafeDarkBrown, shape = cellShape)
                    .then(if (esClickeable) Modifier.clickable { onClick?.invoke() } else Modifier)
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = turno.tipo,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = turno.horario,
                        color = Color(0xFFE5E7EB),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }

                // Pequeño punto naranja en la esquina superior derecha: Modificado tras publicar
                if (turno.modificadoTrasPublicar) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                            .size(6.dp)
                            .background(Color(0xFFF97316), shape = CircleShape)
                    )
                }
            }
        }

        TipoCeldaMalla.DESCANSO -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(Color(0xFFF3F4F6), shape = cellShape)
                    .then(if (esClickeable) Modifier.clickable { onClick?.invoke() } else Modifier),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Descanso",
                    color = Color(0xFF4B5563),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        TipoCeldaMalla.VACACIONES -> {
            // Vacaciones NO es clickeable
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(Color(0xFFE0EDFB), shape = cellShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Vacaciones",
                    color = Color(0xFF1E40AF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        TipoCeldaMalla.INCAPACIDAD -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(Color(0xFFFDE8E8), shape = cellShape)
                    .then(if (esClickeable) Modifier.clickable { onClick?.invoke() } else Modifier),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Incapacidad",
                    color = Color(0xFF991B1B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        TipoCeldaMalla.SIN_ASIGNAR -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .drawBehind {
                        val stroke = Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                        drawRoundRect(
                            color = Color(0xFFD1D5DB),
                            style = stroke,
                            cornerRadius = CornerRadius(6.dp.toPx())
                        )
                    }
                    .then(if (esClickeable) Modifier.clickable { onClick?.invoke() } else Modifier),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+ Asignar",
                    color = Color(0xFF9CA3AF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}


@Composable
fun MallaLeyenda(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Cuadrado marrón oscuro: Turno asignado
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(CafeDarkBrown, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Turno asignado",
                fontSize = 11.sp,
                color = Color(0xFF4B5563)
            )
        }

        // Cuadrado gris claro: Descanso
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(0xFFF3F4F6), RoundedCornerShape(2.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Descanso",
                fontSize = 11.sp,
                color = Color(0xFF4B5563)
            )
        }

        // Cuadrado azul pastel: Vacaciones
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(0xFFE0EDFB), RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Vacaciones",
                fontSize = 11.sp,
                color = Color(0xFF4B5563)
            )
        }

        // Cuadrado rosa pastel: Incapacidad
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(0xFFFDE8E8), RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Incapacidad",
                fontSize = 11.sp,
                color = Color(0xFF4B5563)
            )
        }

        // Punto naranja: Modificado tras publicar
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color(0xFFF97316), shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Modificado tras publicar",
                fontSize = 11.sp,
                color = Color(0xFF4B5563)
            )
        }
    }
}

@Composable
fun MallaEstadoSucursalBadge(
    sucursal: String,
    estadoMalla: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "SUCURSAL $sucursal",
            color = CafeTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(16.dp))

        val colorFondo = if (estadoMalla.contains("BORRADOR", ignoreCase = true)) Color(0xFFFEF3C7) else CafeDarkBrown
        val colorText = if (estadoMalla.contains("BORRADOR", ignoreCase = true)) Color(0xFFB45309) else Color.White

        Box(
            modifier = Modifier
                .background(colorFondo, RoundedCornerShape(4.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = estadoMalla,
                color = colorText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}


@Composable
fun BuscadorEmpleados(
    texto: String,
    onTextoCambiado: (String) -> Unit
){
    OutlinedTextField(
        value = texto,
        onValueChange = onTextoCambiado,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text("Buscar empleado por nombre o cargo...", color = CafeTextSecondary)
        },
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = CafeAccentYellow,
            unfocusedBorderColor = CafeBorderColor,
            focusedTextColor = CafeTextPrimary,
            unfocusedTextColor = CafeTextPrimary
        )
    )
}

@Composable
fun EmpleadoCard(empleado: Empleado){
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CafeBorderColor),
        shape = MaterialTheme.shapes.extraSmall
    ){
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(CafeBackgroundLight),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = empleado.iniciales,
                    fontWeight = FontWeight.Bold,
                    color = CafeDarkBrown,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = empleado.nombre,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${empleado.cargo.displayName} - SUCURSAL ${empleado.sucursal}".uppercase(),
                    fontSize = 10.sp,
                    color = CafeTextSecondary,
                    letterSpacing = 1.sp
                )
            }

            val isActivo = empleado.estado == com.itm.gestordeturnos.domain.model.EstadoUsuario.ACTIVO
            val colorFondoEstado = if (isActivo) CafeStatusSuccessBg else CafeStatusErrorBg
            val colorTextoEstado = if (isActivo) CafeStatusSuccessText else CafeStatusErrorText

            Box(
                modifier = Modifier
                    .background(colorFondoEstado, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ){
                Text(
                    text = empleado.estado.displayName.uppercase(),
                    color = colorTextoEstado,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// Compatibilidad retroactiva con typo
@Composable
fun EmpeladoCard(empleado: Empleado) = EmpleadoCard(empleado)

@Composable
fun SolicitudCambioCard(solicitud: SolicitudCambio, isRolAdmin: Boolean) {
    BoxWithConstraints {
        val isCompact = maxWidth < 600.dp

        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, CafeBorderColor),
            shape = RoundedCornerShape(4.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                // 1. ENCABEZADO
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SucursalChip(
                            sucursal = solicitud.sucursal,
                            borderColor = CafeAccentYellow,
                            textColor = CafeAccentYellow
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("SOLICITUD ${solicitud.numeroSolicitud}", color = CafeTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }

                    val isAceptada = solicitud.estadoResumen == "ACEPTADA POR EL COMPAÑERO"
                    val bgEstado = if (isAceptada) CafeStatusInfoBg else CafeStatusWarningBg
                    val textEstado = if (isAceptada) CafeStatusInfoText else CafeDarkBrown

                    Box(modifier = Modifier.background(bgEstado, RoundedCornerShape(2.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(solicitud.estadoResumen, color = textEstado, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. CAJAS DE INTERCAMBIO
                if (isCompact) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TurnoDetalleBox(modifier = Modifier.fillMaxWidth(), label = "ENTREGA", nombre = solicitud.nombreEntrega, tipo = solicitud.tipoTurnoEntrega, detalle = solicitud.detalleTurnoEntrega)
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Intercambio", tint = CafeAccentYellow, modifier = Modifier.padding(vertical = 16.dp).size(28.dp))
                        TurnoDetalleBox(modifier = Modifier.fillMaxWidth(), label = "RECIBE", nombre = solicitud.nombreRecibe, tipo = solicitud.tipoTurnoRecibe, detalle = solicitud.detalleTurnoRecibe)
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        TurnoDetalleBox(modifier = Modifier.weight(1f), label = "ENTREGA", nombre = solicitud.nombreEntrega, tipo = solicitud.tipoTurnoEntrega, detalle = solicitud.detalleTurnoEntrega)
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Intercambio", tint = CafeAccentYellow, modifier = Modifier.padding(horizontal = 16.dp).size(28.dp))
                        TurnoDetalleBox(modifier = Modifier.weight(1f), label = "RECIBE", nombre = solicitud.nombreRecibe, tipo = solicitud.tipoTurnoRecibe, detalle = solicitud.detalleTurnoRecibe)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = CafeBorderColor)
                Spacer(modifier = Modifier.height(16.dp))

                // 3. MOTIVO
                Text("MOTIVO", color = CafeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(solicitud.motivo, color = CafeTextPrimary, fontSize = 14.sp)

                // 4. CAJA DE VALIDACIONES (SOLO ADMINISTRADOR)
                if (isRolAdmin && solicitud.validacionesOk) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFFF8F9FA)).border(1.dp, CafeBorderColor).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "OK", tint = CafeTextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Validaciones verificadas: descanso mínimo, máximo semanal y sin turnos duplicados.", color = CafeTextSecondary, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                } else {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 5. BOTONES Y ACCIONES DINÁMICAS
                if (isRolAdmin) {
                    if (solicitud.estadoResumen != "APROBADA" && solicitud.estadoResumen != "RECHAZADA") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isCompact) Arrangement.SpaceBetween else Arrangement.Start
                        ) {
                            Button(
                                onClick = { },
                                colors = ButtonDefaults.buttonColors(containerColor = CafeDarkBrown),
                                shape = RoundedCornerShape(4.dp),
                                modifier = if (isCompact) Modifier.weight(1f) else Modifier
                            ) {
                                Text(if (isCompact) "Aprobar" else "Aprobar e intercambiar")
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            OutlinedButton(
                                onClick = { },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CafeTextPrimary),
                                border = BorderStroke(1.dp, CafeBorderColor),
                                shape = RoundedCornerShape(4.dp),
                                modifier = if (isCompact) Modifier.weight(1f) else Modifier
                            ) {
                                Text("Rechazar")
                            }
                        }
                    }
                } else {
                    if (solicitud.esRemitente) {
                        if (solicitud.estadoResumen == "SOLICITADA" || solicitud.estadoResumen == "ACEPTADA POR EL COMPAÑERO") {
                            Text(
                                text = "Cancelar solicitud",
                                color = CafeTextPrimary,
                                fontSize = 14.sp,
                                modifier = Modifier.clickable { }
                            )
                        }
                    } else {
                        if (solicitud.estadoResumen == "SOLICITADA") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isCompact) Arrangement.SpaceBetween else Arrangement.Start
                            ) {
                                Button(
                                    onClick = { },
                                    colors = ButtonDefaults.buttonColors(containerColor = CafeDarkBrown),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = if (isCompact) Modifier.weight(1f) else Modifier
                                ) {
                                    Text("Aceptar", fontWeight = FontWeight.Medium)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                OutlinedButton(
                                    onClick = { },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CafeTextPrimary),
                                    border = BorderStroke(1.dp, CafeBorderColor),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = if (isCompact) Modifier.weight(1f) else Modifier
                                ) {
                                    Text("Rechazar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TurnoDetalleBox(modifier: Modifier, label: String, nombre: String, tipo: String, detalle: String) {
    Column(
        modifier = modifier.background(CafeSurfaceWarm).border(1.dp, CafeBorderLight).padding(16.dp)
    ) {
        Text(label, color = CafeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(nombre, color = CafeTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(tipo, color = CafeTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(detalle, color = CafeTextSecondary, fontSize = 12.sp)
    }
}

@Composable
fun SucursalChip(
    sucursal: String,
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
    textColor: Color? = null
) {
    val esCentro = sucursal.equals("CENTRO", ignoreCase = true)
    val esNorte = sucursal.equals("NORTE", ignoreCase = true)

    val finalTextColor = textColor ?: when {
        esCentro -> Color(0xFFD97706)
        esNorte -> Color(0xFF2563EB)
        else -> CafeTextSecondary
    }

    val finalBorderColor = borderColor ?: when {
        esCentro -> Color(0xFFFDE68A)
        esNorte -> Color(0xFFBFDBFE)
        else -> Color(0xFFD6D6D6)
    }

    Box(
        modifier = modifier
            .border(1.dp, finalBorderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = sucursal.uppercase(),
            color = finalTextColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun NovedadEstadoBadge(
    estado: com.itm.gestordeturnos.domain.model.EstadoNovedad,
    modifier: Modifier = Modifier
) {
    NovedadEstadoBadge(estado = estado.name, modifier = modifier)
}

@Composable
fun NovedadEstadoBadge(
    estado: String,
    modifier: Modifier = Modifier
) {
    val isRevisada = estado.equals("REVISADA", ignoreCase = true)
    val bgEstado = if (isRevisada) CafeStatusSuccessBg else CafeStatusWarningBg
    val textEstado = if (isRevisada) CafeStatusSuccessText else CafeDarkBrown

    Box(
        modifier = modifier
            .background(bgEstado, RoundedCornerShape(2.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = estado.uppercase(),
            color = textEstado,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}
/**
 * Dropdown selector unificado y genérico para filtros, selecciones y formularios.
 */
@Composable
fun <T> AppDropdown(
    valorActual: T,
    opciones: List<T>,
    onSeleccion: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    formatoTexto: (T) -> String = { it.toString() },
    formatoItemMenu: ((T) -> String)? = null,
    altura: Dp = 48.dp,
    shape: RoundedCornerShape = RoundedCornerShape(2.dp),
    colorBorde: Color = CafeBorderColor,
    colorFondo: Color = Color.White,
    textoSize: TextUnit = 13.sp,
    isCompact: Boolean = false
) {
    var expandido by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        if (!label.isNullOrBlank()) {
            Text(
                text = label.uppercase(),
                fontSize = if (isCompact) 10.sp else 11.sp,
                fontWeight = FontWeight.Bold,
                color = CafeTextSecondary,
                letterSpacing = if (isCompact) 0.5.sp else 1.sp
            )
            Spacer(modifier = Modifier.height(if (isCompact) 4.dp else 6.dp))
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(altura)
                    .background(colorFondo, shape)
                    .border(1.dp, colorBorde, shape)
                    .clickable { expandido = true }
                    .padding(horizontal = if (isCompact) 8.dp else 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatoTexto(valorActual),
                    fontSize = textoSize,
                    fontWeight = FontWeight.Medium,
                    color = CafeTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = label ?: "Seleccionar opción",
                    tint = CafeTextSecondary,
                    modifier = if (isCompact) Modifier.size(16.dp) else Modifier.size(24.dp)
                )
            }

            DropdownMenu(
                expanded = expandido,
                onDismissRequest = { expandido = false },
                modifier = Modifier.background(Color.White)
            ) {
                opciones.forEach { opcion ->
                    val isSelected = opcion == valorActual
                    val displayText = formatoItemMenu?.invoke(opcion) ?: formatoTexto(opcion)
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = displayText,
                                fontSize = if (isCompact) 12.sp else 13.sp,
                                color = CafeTextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onSeleccion(opcion)
                            expandido = false
                        },
                        modifier = Modifier.background(
                            if (isSelected) CafeBackgroundLight else Color.Transparent
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun FiltroDropdown(
    opcionSeleccionada: String,
    opciones: List<String>,
    onOpcionSeleccionada: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AppDropdown(
        valorActual = opcionSeleccionada,
        opciones = opciones,
        onSeleccion = onOpcionSeleccionada,
        modifier = modifier.width(130.dp),
        altura = 40.dp,
        colorBorde = CafeBorderColor,
        shape = RoundedCornerShape(4.dp),
        textoSize = 14.sp
    )
}
@Composable
fun EncabezadoPantalla(
    titulo: String,
    subtitulo: String? = null,
    isCompact: Boolean = false,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    modifier: Modifier = Modifier,
    accion: (@Composable () -> Unit)? = null
) {
    if (isCompact) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = horizontalAlignment
        ) {
            Text(
                text = titulo,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = CafeTextPrimary,
                textAlign = if (horizontalAlignment == Alignment.CenterHorizontally) TextAlign.Center else TextAlign.Start
            )
            if (subtitulo != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subtitulo,
                    fontSize = 14.sp,
                    color = CafeTextSecondary,
                    textAlign = if (horizontalAlignment == Alignment.CenterHorizontally) TextAlign.Center else TextAlign.Start
                )
            }
            if (accion != null) {
                Spacer(modifier = Modifier.height(16.dp))
                accion()
            }
        }
    } else {
        if (horizontalAlignment == Alignment.CenterHorizontally) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = titulo,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeTextPrimary,
                    textAlign = TextAlign.Center
                )
                if (subtitulo != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = subtitulo,
                        fontSize = 16.sp,
                        color = CafeTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
                if (accion != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    accion()
                }
            }
        } else {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = titulo,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = CafeTextPrimary
                    )
                    if (subtitulo != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = subtitulo,
                            fontSize = 14.sp,
                            color = CafeTextSecondary
                        )
                    }
                }
                if (accion != null) {
                    accion()
                }
            }
        }
    }
}

@Composable
fun NovedadCard(novedad: Novedad) {
    val colorBarra = when (novedad.colorBorde.ifBlank { novedad.colorVorde }.uppercase()) {
        "ROJO" -> Color(0xFFAC3927)
        "VERDE", "VERDE_OSCURO" -> Color(0xFF437637)
        "GRIS", "GRIS_OSCURO" -> Color(0xFF5A5A5A)
        "ROJO_OSCURO", "MARRON", "VERDE_CLARO" -> Color(0xFF78281F)
        else -> Color(0xFF5A5A5A)
    }

    BoxWithConstraints {
        val isCompact = maxWidth < 600.dp
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, CafeBorderColor),
            shape = RoundedCornerShape(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Borde izquierdo de color solido que indica su gravedad o tipo
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(colorBarra)
                )

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = if (isCompact) Alignment.Top else Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        if (isCompact) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = novedad.tipo,
                                        color = CafeTextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (novedad.empleado.isNotBlank()) {
                                        Text(
                                            text = " \u00B7 " + novedad.empleado,
                                            color = CafeTextPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                }
                                if (novedad.sucursal.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    SucursalChip(sucursal = novedad.sucursal)
                                }
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = novedad.tipo,
                                        color = CafeTextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (novedad.empleado.isNotBlank()) {
                                        Text(
                                            text = " \u00B7 " + novedad.empleado,
                                            color = CafeTextPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                }
                                if (novedad.sucursal.isNotBlank()) {
                                    SucursalChip(sucursal = novedad.sucursal)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Subtitulo 1: Turno y Fecha (Gris medio)
                        if (novedad.detalleTurno.isNotBlank()) {
                            Text(
                                text = novedad.detalleTurno,
                                color = CafeTextSecondary,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Subtitulo 2: Diferencia horaria (Gris oscuro, fuente monoespaciada)
                        if (novedad.diferencia.isNotBlank()) {
                            Text(
                                text = novedad.diferencia,
                                color = Color(0xFF333333),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        if (isCompact) {
                            Spacer(modifier = Modifier.height(12.dp))
                            NovedadEstadoBadge(estado = novedad.estado)
                        }
                    }

                    if (!isCompact) {
                        Spacer(modifier = Modifier.width(16.dp))
                        NovedadEstadoBadge(estado = novedad.estado)
                    }
                }
            }
        }
    }
}
@Composable
fun ModalSolicitarCambio(
    turnosPropios: List<TurnoSeleccionable>,
    turnosCompaneros: List<TurnoCompaneroSeleccionable>,
    turnoSeleccionado: TurnoSeleccionable?,
    onTurnoPropioSeleccionado: (TurnoSeleccionable) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // ENCABEZADO
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text("Solicitar cambio de turno", color = CafeTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = CafeTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // CONTENIDO SCROLLEABLE
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // SECCIÓN 1: TURNO QUE ENTREGAS
                    Text("1 · TURNO QUE ENTREGAS", color = CafeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    turnosPropios.forEach { turno ->
                        val isSelected = turno == turnoSeleccionado
                        val borderColor = if (isSelected) CafeDarkBrown else CafeBorderColor
                        val borderWidth = if (isSelected) 2.dp else 1.dp

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .border(borderWidth, borderColor, RoundedCornerShape(2.dp))
                                .clickable { onTurnoPropioSeleccionado(turno) }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(turno.descripcion, color = CafeTextPrimary, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            Text(turno.horario, color = CafeTextSecondary, fontSize = 12.sp)
                        }
                    }

                    // SECCIÓN 2: TURNO QUE RECIBES
                    AnimatedVisibility(visible = turnoSeleccionado != null) {
                        Column(modifier = Modifier.padding(top = 24.dp)) {
                            Text("2 · TURNO DEL COMPAÑERO QUE RECIBES", color = CafeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Solo se listan Baristas de Centro sin conflicto de agenda.", color = CafeTextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            turnosCompaneros.forEach { companero ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                        .border(1.dp, CafeBorderColor, RoundedCornerShape(2.dp))
                                        .clickable { }
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(companero.nombre, color = CafeTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(companero.descripcion, color = CafeTextSecondary, fontSize = 12.sp)
                                    }
                                    Text(companero.horario, color = CafeTextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class SelectorHoraTarget(
    val titulo: String,
    val horaActual: String,
    val onConfirmar: (String) -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogTimePicker(
    titulo: String,
    horaInicial: String,
    onConfirmar: (String) -> Unit,
    onDescartar: () -> Unit
) {
    val (h, m) = remember(horaInicial) {
        val partes = horaInicial.split(":")
        val hora = partes.getOrNull(0)?.toIntOrNull() ?: 8
        val min = partes.getOrNull(1)?.toIntOrNull() ?: 0
        hora.coerceIn(0, 23) to min.coerceIn(0, 59)
    }

    val timePickerState = rememberTimePickerState(
        initialHour = h,
        initialMinute = m,
        is24Hour = true
    )

    Dialog(onDismissRequest = onDescartar) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CafeSurfaceWarm),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = titulo.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeTextSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = Color(0xFFF7F4F0),
                        selectorColor = CafeDarkBrown,
                        periodSelectorBorderColor = CafeDarkBrown,
                        periodSelectorSelectedContainerColor = CafeDarkBrown,
                        periodSelectorSelectedContentColor = Color.White,
                        timeSelectorSelectedContainerColor = CafeDarkBrown,
                        timeSelectorSelectedContentColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDescartar) {
                        Text("Cancelar", color = CafeTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val horaStr = timePickerState.hour.toString().padStart(2, '0')
                            val minStr = timePickerState.minute.toString().padStart(2, '0')
                            onConfirmar("$horaStr:$minStr")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CafeDarkBrown),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Aceptar", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ModalCrearTurno(
    nombre: String,
    horaInicio: String,
    horaFin: String,
    descansos: List<RangoDescanso>,
    mensajeError: String? = null,
    horasEfectivas: String = "",
    esEdicion: Boolean = false,
    estado: String = "ACTIVO",
    onNombreChange: (String) -> Unit,
    onHoraInicioChange: (String) -> Unit,
    onHoraFinChange: (String) -> Unit,
    onEstadoChange: (String) -> Unit = {},
    onAgregarDescanso: () -> Unit,
    onQuitarDescanso: (String) -> Unit,
    onDescansoInicioChange: (String, String) -> Unit,
    onDescansoFinChange: (String, String) -> Unit,
    onGuardar: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectorHoraActivo by remember { mutableStateOf<SelectorHoraTarget?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f),
            colors = CardDefaults.cardColors(containerColor = CafeSurfaceWarm),
            shape = RoundedCornerShape(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // ENCABEZADO
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(if (esEdicion) "EDITAR TURNO" else "NUEVO TURNO", color = CafeTextSecondary, fontSize = 10.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(if (esEdicion) "Modificar turno" else "Crear turno", color = CafeTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = CafeTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // MENSAJE DE ERROR
                if (mensajeError != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CafeStatusErrorBg, RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFFF5C6CB), RoundedCornerShape(4.dp))
                            .padding(12.dp)
                    ) {
                        Text(mensajeError, color = CafeStatusErrorText, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // CONTENIDO SCROLLEABLE
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {

                    // ESTADO DEL TURNO
                    Text("ESTADO DEL TURNO", color = CafeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf("ACTIVO", "INACTIVO").forEach { opcion ->
                            val seleccionado = estado == opcion
                            val bgChip = if (seleccionado) {
                                if (opcion == "ACTIVO") CafeStatusSuccessBg else CafeStatusErrorBg
                            } else Color.White
                            val textChip = if (seleccionado) {
                                if (opcion == "ACTIVO") CafeStatusSuccessText else CafeStatusErrorText
                            } else CafeTextSecondary
                            val borderChip = if (seleccionado) {
                                if (opcion == "ACTIVO") CafeStatusSuccessText else CafeStatusErrorText
                            } else CafeBorderColor

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(bgChip, RoundedCornerShape(4.dp))
                                    .border(1.dp, borderChip, RoundedCornerShape(4.dp))
                                    .clickable { onEstadoChange(opcion) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                if (opcion == "ACTIVO") CafeStatusSuccessText else CafeStatusErrorText,
                                                CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = opcion,
                                        color = textChip,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // NOMBRE DEL TURNO
                    Text("NOMBRE DEL TURNO", color = CafeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = onNombreChange,
                        placeholder = { Text("p. ej. Apertura", color = CafeTextSecondary, fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColorsGestor()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // HORA DE INICIO Y FIN
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("HORA DE INICIO", color = CafeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            TimeFieldBox(
                                valor = horaInicio,
                                onClick = {
                                    selectorHoraActivo = SelectorHoraTarget("Hora de inicio", horaInicio) {
                                        onHoraInicioChange(it)
                                    }
                                }
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("HORA DE FIN", color = CafeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            TimeFieldBox(
                                valor = horaFin,
                                onClick = {
                                    selectorHoraActivo = SelectorHoraTarget("Hora de fin", horaFin) {
                                        onHoraFinChange(it)
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // SECCIÓN DESCANSOS (Dinámica)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("DESCANSOS (MÍN. 15 MIN)", color = CafeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Text("Máx. cada 2h 45m de trabajo", color = CafeTextSecondary, fontSize = 10.sp)
                        }
                        Text(
                            text = "+ AÑADIR",
                            color = CafeAccentYellow,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.clickable { onAgregarDescanso() }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (descansos.isEmpty()) {
                        Text(
                            "Sin descansos configurados (se calcularán automáticamente al indicar inicio y fin).",
                            color = CafeTextSecondary,
                            fontSize = 12.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    } else {
                        descansos.forEach { descanso ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    TimeFieldBox(
                                        valor = descanso.horaInicio,
                                        onClick = {
                                            selectorHoraActivo = SelectorHoraTarget("Inicio de descanso", descanso.horaInicio) {
                                                onDescansoInicioChange(descanso.id, it)
                                            }
                                        }
                                    )
                                }
                                Text("-", color = CafeTextSecondary)
                                Box(modifier = Modifier.weight(1f)) {
                                    TimeFieldBox(
                                        valor = descanso.horaFin,
                                        onClick = {
                                            selectorHoraActivo = SelectorHoraTarget("Fin de descanso", descanso.horaFin) {
                                                onDescansoFinChange(descanso.id, it)
                                            }
                                        }
                                    )
                                }
                                IconButton(onClick = { onQuitarDescanso(descanso.id) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Quitar descanso", tint = CafeTextSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // RESUMEN HORAS EFECTIVAS
                    if (horasEfectivas.isNotBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFAF6F0), RoundedCornerShape(4.dp))
                                .border(1.dp, Color(0xFFE8DFD5), RoundedCornerShape(4.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("TIEMPO LABORABLE EFECTIVO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CafeTextSecondary, letterSpacing = 1.sp)
                            Text(horasEfectivas, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CafeDarkBrown)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // BOTONES DE ACCIÓN
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onGuardar,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CafeDarkBrown),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(if (esEdicion) "Guardar cambios" else "Guardar", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, CafeBorderColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CafeTextPrimary)
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }

    // Modal TimePicker
    selectorHoraActivo?.let { target ->
        DialogTimePicker(
            titulo = target.titulo,
            horaInicial = target.horaActual,
            onConfirmar = { horaSeleccionada ->
                target.onConfirmar(horaSeleccionada)
                selectorHoraActivo = null
            },
            onDescartar = {
                selectorHoraActivo = null
            }
        )
    }
}
// Componente auxiliar para los campos de hora visuales
@Composable
fun TimeFieldBox(valor: String, onClick: () -> Unit) {
    val displayValue = valor.ifEmpty { "--:-- -----" }
    val textColor = if (valor.isEmpty()) CafeTextSecondary else CafeTextPrimary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(Color.Transparent)
            .border(1.dp, CafeBorderColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(displayValue, color = textColor, fontSize = 14.sp)
        Icon(Icons.Outlined.Schedule, contentDescription = "Seleccionar hora", tint = CafeTextSecondary, modifier = Modifier.size(18.dp))
    }
}

// Colores unificados para el OutlinedTextField
@Composable
fun textFieldColorsGestor() = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = Color.Transparent,
    focusedContainerColor = Color.White,
    unfocusedBorderColor = CafeBorderColor,
    focusedBorderColor = CafeDarkBrown,
    unfocusedTextColor = CafeTextPrimary,
    focusedTextColor = CafeTextPrimary
)