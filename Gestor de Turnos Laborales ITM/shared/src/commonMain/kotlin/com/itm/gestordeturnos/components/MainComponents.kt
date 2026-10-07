package com.itm.gestordeturnos.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itm.gestordeturnos.CafeAccentYellow
import com.itm.gestordeturnos.CafeBackgroundLight
import com.itm.gestordeturnos.CafeBorderColor
import com.itm.gestordeturnos.CafeDarkBrown
import com.itm.gestordeturnos.CafeTextPrimary
import com.itm.gestordeturnos.CafeTextSecondary
import com.itm.gestordeturnos.viewmodel.Empleado
import com.itm.gestordeturnos.viewmodel.FilaMalla
import com.itm.gestordeturnos.viewmodel.Novedad
import com.itm.gestordeturnos.viewmodel.SolicitudCambio
import com.itm.gestordeturnos.viewmodel.TurnoAsignado
import com.itm.gestordeturnos.viewmodel.TurnoCelda

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
    // Color del borde (Amarillo si es descanso, gris si no)
    val colorBorde = if (turno.esDescanso) CafeAccentYellow else Color(0xFFE0E0E0)

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, colorBorde),
        shape = RoundedCornerShape(2.dp) // Bordes casi cuadrados
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min) // Crítico: iguala el alto de la línea separadora
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
                    .background(Color(0xFFE0E0E0))
            )
            Spacer(modifier = Modifier.width(16.dp))

            // 3. Columna Central: Apertura / Cierre / Descanso
            Column(modifier = Modifier.weight(1f)) {
                when {
                    turno.esDescanso -> {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFD3E3FD), RoundedCornerShape(2.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "DESCANSO",
                                color = Color(0xFF0B57D0),
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

                            // Etiqueta de "MODIFICADO"
                            if (turno.etiquetaModificado != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFF3E7C9), RoundedCornerShape(2.dp))
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
fun MallaHeaderRow(dias: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CafeDarkBrown)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "EMPLEADO",
            color = CafeAccentYellow,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(150.dp)
        )

        dias.forEach { dia ->
            Text(
                text = dia.uppercase(),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = "TOTAL",
            color = CafeAccentYellow,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(60.dp)
        )
    }
}

@Composable
fun MallaEmployeeRow(fila: FilaMalla) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(width = 1.dp, color = CafeBorderColor)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.width(150.dp)) {
            Text(
                text = fila.nombre,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = CafeTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = fila.cargo,
                fontSize = 10.sp,
                color = CafeTextSecondary,
                letterSpacing = 1.sp
            )
        }

        fila.turnos.forEach { turno ->
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                MallaCeldaTurno(turno)
            }
        }

        Text(
            text = fila.totalHoras,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = CafeDarkBrown,
            textAlign = TextAlign.End,
            modifier = Modifier.width(60.dp)
        )
    }
}

@Composable
fun MallaCeldaTurno(turno: TurnoCelda) {
    if (turno.esDescanso) {
        Box(
            modifier = Modifier
                .background(Color(0xFFF0F0F0))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text("DESC", fontSize = 10.sp, color = CafeTextSecondary)
        }
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = turno.tipo,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CafeTextPrimary
            )
            Text(
                text = turno.horario,
                fontSize = 10.sp,
                color = CafeTextSecondary
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

        val colorFondo = if (estadoMalla == "BORRADOR") CafeAccentYellow else CafeDarkBrown
        val colorText = if (estadoMalla == "BORRADOR") CafeDarkBrown else Color.White

        Box(
            modifier = Modifier
                .background(colorFondo)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = estadoMalla,
                color = colorText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
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
fun EmpeladoCard(empleado: Empleado){
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp,CafeBorderColor),
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
                    text = "${empleado.cargo} - SUCURSAL ${empleado.sucursal}".uppercase(),
                    fontSize = 10.sp,
                    color = CafeTextSecondary,
                    letterSpacing = 1.sp
                )
            }

            val isActivo = empleado.estado == "ACTIVO"
            val colorFondoEstado = if (isActivo) Color(0xFFE6F4EA) else Color(0xFFFCE8E6)
            val colorTextoEstado = if (isActivo) Color(0xFF1E8E3E) else Color(0xFFD93025)

            Box(
                modifier = Modifier
                    .background(colorFondoEstado, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ){
                Text(
                    text = empleado.estado,
                    color = colorTextoEstado,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun SolicitudCambioCard(solicitud: SolicitudCambio, isRolAdmin: Boolean) {
    BoxWithConstraints {
        val isCompact = maxWidth < 600.dp

        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
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
                    val bgEstado = if (isAceptada) Color(0xFFD3E3FD) else Color(0xFFF3E7C9) // Azul o Amarillo suave
                    val textEstado = if (isAceptada) Color(0xFF0B57D0) else CafeDarkBrown

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
                HorizontalDivider(color = Color(0xFFE0E0E0))
                Spacer(modifier = Modifier.height(16.dp))

                // 3. MOTIVO
                Text("MOTIVO", color = CafeTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(solicitud.motivo, color = CafeTextPrimary, fontSize = 14.sp)

                // 4. CAJA DE VALIDACIONES (SOLO ADMINISTRADOR)
                if (isRolAdmin && solicitud.validacionesOk) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFFF8F9FA)).border(1.dp, Color(0xFFE0E0E0)).padding(12.dp),
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
                    // VISTA ADMINISTRADOR
                    // El admin solo aprueba/rechaza si no ha sido resuelta definitivamente
                    if (solicitud.estadoResumen != "APROBADA" && solicitud.estadoResumen != "RECHAZADA") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isCompact) Arrangement.SpaceBetween else Arrangement.Start
                        ) {
                            Button(
                                onClick = { /* Lógica aprobar */ },
                                colors = ButtonDefaults.buttonColors(containerColor = CafeDarkBrown),
                                shape = RoundedCornerShape(4.dp),
                                modifier = if (isCompact) Modifier.weight(1f) else Modifier
                            ) {
                                Text(if (isCompact) "Aprobar" else "Aprobar e intercambiar")
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            OutlinedButton(
                                onClick = { /* Lógica rechazar */ },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CafeTextPrimary),
                                border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                                shape = RoundedCornerShape(4.dp),
                                modifier = if (isCompact) Modifier.weight(1f) else Modifier
                            ) {
                                Text("Rechazar")
                            }
                        }
                    }
                } else {
                    // VISTA EMPLEADO
                    if (solicitud.esRemitente) {
                        // El remitente puede "Cancelar" mientras el administrador no la haya cerrado
                        if (solicitud.estadoResumen == "SOLICITADA" || solicitud.estadoResumen == "ACEPTADA POR EL COMPAÑERO") {
                            Text(
                                text = "Cancelar solicitud",
                                color = CafeTextPrimary,
                                fontSize = 14.sp,
                                modifier = Modifier.clickable { /* Lógica cancelar */ }
                            )
                        }
                    } else {
                        // CRÍTICO: El receptor SOLO puede aceptar/rechazar si está "SOLICITADA"
                        if (solicitud.estadoResumen == "SOLICITADA") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isCompact) Arrangement.SpaceBetween else Arrangement.Start
                            ) {
                                Button(
                                    onClick = { /* Lógica aceptar */ },
                                    colors = ButtonDefaults.buttonColors(containerColor = CafeDarkBrown),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = if (isCompact) Modifier.weight(1f) else Modifier
                                ) {
                                    Text("Aceptar", fontWeight = FontWeight.Medium)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                OutlinedButton(
                                    onClick = { /* Lógica rechazar */ },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CafeTextPrimary),
                                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
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
        modifier = modifier.background(Color(0xFFFAF9F6)).border(1.dp, Color(0xFFEAEAEA)).padding(16.dp)
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
    borderColor: Color = Color(0xFFD6D6D6),
    textColor: Color = CafeTextSecondary
) {
    Box(
        modifier = modifier
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = sucursal,
            color = textColor,
            fontSize = 10.sp,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun NovedadEstadoBadge(
    estado: String,
    modifier: Modifier = Modifier
) {
    val isRevisada = estado == "REVISADA"
    val bgEstado = if (isRevisada) Color(0xFFDFEADB) else Color(0xFFEFE2C5)
    val textEstado = if (isRevisada) Color(0xFF4C7B4C) else CafeDarkBrown

    Box(
        modifier = modifier
            .background(bgEstado, RoundedCornerShape(2.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = estado,
            color = textEstado,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun FiltroDropdown(
    opcionSeleccionada: String,
    opciones: List<String>,
    onOpcionSeleccionada: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpandido by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clickable { menuExpandido = true }
                .border(1.dp, CafeTextPrimary, RoundedCornerShape(4.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                opcionSeleccionada,
                color = CafeTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = "Filtrar",
                tint = CafeTextPrimary
            )
        }

        DropdownMenu(
            expanded = menuExpandido,
            onDismissRequest = { menuExpandido = false },
            modifier = Modifier.background(Color.White)
        ) {
            opciones.forEach { opcion ->
                val isSelected = opcionSeleccionada == opcion
                DropdownMenuItem(
                    text = {
                        Text(
                            text = opcion,
                            color = if (isSelected) Color.White else CafeTextPrimary
                        )
                    },
                    onClick = {
                        onOpcionSeleccionada(opcion)
                        menuExpandido = false
                    },
                    modifier = Modifier.background(if (isSelected) Color(0xFF757575) else Color.Transparent)
                )
            }
        }
    }
}

@Composable
fun EncabezadoPantalla(
    titulo: String,
    subtitulo: String? = null,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier,
    accion: (@Composable () -> Unit)? = null
) {
    if (isCompact) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                titulo,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = CafeTextPrimary
            )
            if (subtitulo != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    subtitulo,
                    fontSize = 14.sp,
                    color = CafeTextSecondary
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
                    titulo,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeTextPrimary
                )
                if (subtitulo != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        subtitulo,
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

@Composable
fun NovedadCard(novedad: Novedad){
    val colorBarra = when (novedad.colorVorde){
        "ROJO" -> Color(0xFFAC3927)
        "VERDE" -> Color(0xFF437637)
        "VERDE_CLARO" -> Color(0xFF9E3422)
        else -> Color(0xFF8C8C8C)
    }

    BoxWithConstraints {
        val isCompact = maxWidth < 500.dp
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
            shape = RoundedCornerShape(2.dp)
        ){
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(16.dp),
                verticalAlignment = if (isCompact) Alignment.Top else Alignment.CenterVertically
            ){
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .background(colorBarra)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            novedad.tipo,
                            color = CafeTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (!isCompact) {
                            Text(
                                " - ${novedad.empleado} ",
                                color = CafeTextSecondary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            SucursalChip(sucursal = novedad.sucursal)
                        }
                    }
                    
                    if (isCompact) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                novedad.empleado,
                                color = CafeTextSecondary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            SucursalChip(sucursal = novedad.sucursal)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        novedad.detalleTurno,
                        color = CafeTextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        novedad.diferencia,
                        color = CafeTextPrimary,
                        fontSize = 12.sp
                    )

                    if (isCompact) {
                        Spacer(modifier = Modifier.height(12.dp))
                        NovedadEstadoBadge(estado = novedad.estado)
                    }
                }

                if (!isCompact) {
                    NovedadEstadoBadge(estado = novedad.estado)
                }
            }
        }
    }
}

