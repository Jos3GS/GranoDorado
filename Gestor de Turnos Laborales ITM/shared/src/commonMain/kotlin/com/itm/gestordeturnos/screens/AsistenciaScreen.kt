package com.itm.gestordeturnos.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.itm.gestordeturnos.*
import com.itm.gestordeturnos.components.EncabezadoPantalla
import com.itm.gestordeturnos.components.SucursalChip
import com.itm.gestordeturnos.domain.model.RegistroAsistencia
import com.itm.gestordeturnos.viewmodel.AsistenciaViewModel

@Composable
fun AsistenciaScreen(viewModel: AsistenciaViewModel) {
    val state by viewModel.uiState.collectAsState()

    // Scroll vertical general de la pantalla
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {

        // 1. ENCABEZADO
        EncabezadoPantalla(
            titulo = "Control de asistencia",
            subtitulo = "Estado de las marcaciones del día y notificaciones de incumplimientos."
        )

        // 2. PANEL DE INCUMPLIMIENTOS (Borde izquierdo rojo oscuro)
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = CafeSurfaceWarm),
            border = BorderStroke(1.dp, CafeBorderLight),
            shape = RoundedCornerShape(2.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                // Línea roja indicadora lateral
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF9E3422))
                )

                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "NOTIFICACIONES DE INCUMPLIMIENTO",
                        color = CafeTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    state.notificaciones.forEach { notificacion ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).background(CafeAccentYellow, CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))

                            // Texto compuesto: Empleado (Bold) + Tipo (Normal) + Hora
                            Text(
                                text = notificacion.empleado,
                                fontWeight = FontWeight.Bold,
                                color = CafeTextPrimary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${notificacion.tipo} ${notificacion.detalleHora}".trim(),
                                color = CafeTextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // 3. TABLA DE ASISTENCIA
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isDesktop = maxWidth > 800.dp

            if (isDesktop) {
                // VERSIÓN ESCRITORIO: Ocupa todo el ancho sin scroll horizontal
                TablaAsistencia(
                    registros = state.registrosDiarios,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // VERSIÓN MÓVIL: Usa scroll horizontal y un ancho fijo de 800.dp para respirar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    TablaAsistencia(
                        registros = state.registrosDiarios,
                        modifier = Modifier.width(800.dp)
                    )
                }
            }
        }
    }
}

// COMPONENTES DE LA TABLA
@Composable
private fun TablaAsistencia(registros: List<RegistroAsistencia>, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CafeBorderLight),
        shape = RoundedCornerShape(2.dp),
        modifier = modifier
    ) {
        Column {
            // Encabezados
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CabeceraTablaAsis("EMPLEADO", modifier = Modifier.weight(2f))
                CabeceraTablaAsis("SUCURSAL", modifier = Modifier.weight(1.5f))
                CabeceraTablaAsis("ENTRADA", modifier = Modifier.weight(1.5f))
                CabeceraTablaAsis("SALIDA", modifier = Modifier.weight(1.5f))
                CabeceraTablaAsis("ESTADO", modifier = Modifier.weight(1.5f))
            }
            HorizontalDivider(color = CafeBorderLight)

            // Filas
            registros.forEach { registro ->
                FilaAsistencia(registro)
                HorizontalDivider(color = CafeBorderLight)
            }
        }
    }
}

@Composable
private fun CabeceraTablaAsis(texto: String, modifier: Modifier) {
    Text(
        text = texto,
        modifier = modifier,
        color = CafeTextSecondary,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
}

@Composable
private fun FilaAsistencia(registro: RegistroAsistencia) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Columna Empleado
        Text(registro.empleado, modifier = Modifier.weight(2f), color = CafeTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

        // Columna Sucursal
        Box(modifier = Modifier.weight(1.5f)) {
            SucursalChip(sucursal = registro.sucursal)
        }

        // Columna Entrada
        Text(registro.horaEntrada, modifier = Modifier.weight(1.5f), color = CafeTextPrimary, fontSize = 14.sp)

        // Columna Salida
        Text(registro.horaSalida, modifier = Modifier.weight(1.5f), color = CafeTextPrimary, fontSize = 14.sp)

        // Columna Estado (Tokens semánticos de Theme)
        Box(modifier = Modifier.weight(1.5f)) {
            val bgEstado = when (registro.estado) {
                "PRESENTE" -> CafeStatusSuccessBg
                "TARDE" -> CafeStatusWarningBg
                else -> CafeStatusErrorBg
            }
            val textEstado = when (registro.estado) {
                "PRESENTE" -> CafeStatusSuccessText
                "TARDE" -> CafeStatusWarningText
                else -> CafeStatusErrorText
            }

            Box(modifier = Modifier.background(bgEstado, RoundedCornerShape(2.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(registro.estado, color = textEstado, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
    }
}
