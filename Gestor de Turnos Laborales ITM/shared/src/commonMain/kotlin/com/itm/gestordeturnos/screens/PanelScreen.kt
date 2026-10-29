package com.itm.gestordeturnos.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.itm.gestordeturnos.CafeAccentYellow
import com.itm.gestordeturnos.CafeBackgroundLight
import com.itm.gestordeturnos.CafeBorderColor
import com.itm.gestordeturnos.CafeDarkBrown
import com.itm.gestordeturnos.CafeStatusErrorBg
import com.itm.gestordeturnos.CafeStatusErrorText
import com.itm.gestordeturnos.CafeStatusInfoBg
import com.itm.gestordeturnos.CafeStatusInfoText
import com.itm.gestordeturnos.CafeStatusSuccessBg
import com.itm.gestordeturnos.CafeStatusSuccessText
import com.itm.gestordeturnos.CafeStatusWarningBg
import com.itm.gestordeturnos.CafeTextPrimary
import com.itm.gestordeturnos.CafeTextSecondary
import com.itm.gestordeturnos.components.PrimarySolidButton
import com.itm.gestordeturnos.components.SucursalChip
import com.itm.gestordeturnos.domain.model.RolUsuario
import com.itm.gestordeturnos.domain.model.UsuarioActual
import com.itm.gestordeturnos.viewmodel.PanelState
import com.itm.gestordeturnos.viewmodel.PanelViewModel
import com.itm.gestordeturnos.viewmodel.ResumenAsistenciaHoy
import com.itm.gestordeturnos.viewmodel.SucursalMetrica
import com.itm.gestordeturnos.viewmodel.TareaRonda

@Composable
fun PanelScreen(
    usuario: UsuarioActual,
    viewModel: PanelViewModel = viewModel { PanelViewModel() },
    onNavigate: (String) -> Unit
) {
    LaunchedEffect(usuario) {
        viewModel.cargarDatos(usuario)
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        val isCompact = maxWidth < 800.dp
        val statCardWidth = if (isCompact) (maxWidth / 2) - 10.dp else 190.dp

        Column(modifier = Modifier.fillMaxWidth()) {
            GreetingHeader(
                saludo = state.saludo,
                nombre = usuario.nombre,
                rol = usuario.cargo,
                scope = if (usuario.rol == RolUsuario.ADMINISTRADOR_GENERAL) "VISTA CONSOLIDADA (3 SUCURSALES)" else "SUCURSAL ${usuario.sucursal.ifBlank { "CENTRO" }}",
                fechaHoy = state.fechaHoyTexto,
                horaActual = state.horaActualTexto,
                isCompact = isCompact
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de Sucursales para Administrador General
            if (state.esAdminGeneral) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "FILTRAR SUCURSAL:",
                        color = CafeTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    state.sucursalesDisponibles.forEach { suc ->
                        val isSelected = state.sucursalSeleccionada.equals(suc, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) CafeDarkBrown else Color.White,
                                    RoundedCornerShape(14.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) CafeDarkBrown else CafeBorderColor,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { viewModel.cambiarFiltroSucursal(suc) }
                                .padding(horizontal = 14.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = suc,
                                color = if (isSelected) Color.White else CafeTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // StatCards Grid (Métricas en Tiempo Real)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                StatCard(
                    numero = state.empleadosActivos.toString(),
                    etiqueta = "EMPLEADOS ACTIVOS",
                    modifier = Modifier.width(statCardWidth),
                    onClick = { onNavigate("empleados") }
                )
                StatCard(
                    numero = state.enTurnoAhoraTotal.toString(),
                    etiqueta = "EN TURNO AHORA",
                    subetiqueta = "De ${state.turnosHoyTotal} turnos hoy",
                    badge = "EN VIVO",
                    badgeColor = Color(0xFF16A34A),
                    modifier = Modifier.width(statCardWidth),
                    onClick = { onNavigate("asistencia") }
                )
                StatCard(
                    numero = state.novedadesPendientes.toString(),
                    etiqueta = "NOVEDADES PENDIENTES",
                    subetiqueta = if (state.novedadesPendientes > 0) "Esperan revisión" else "Al día",
                    colorNumero = if (state.novedadesPendientes > 0) Color(0xFFB45309) else Color(0xFFA04E35),
                    modifier = Modifier.width(statCardWidth),
                    onClick = { onNavigate("novedades") }
                )
                StatCard(
                    numero = state.cambiosPorAprobar.toString(),
                    etiqueta = "CAMBIOS POR APROBAR",
                    subetiqueta = if (state.cambiosPorAprobar > 0) "Por decidir" else "Sin pendientes",
                    colorNumero = if (state.cambiosPorAprobar > 0) Color(0xFF2563EB) else Color(0xFFA04E35),
                    modifier = Modifier.width(statCardWidth),
                    onClick = { onNavigate("cambios") }
                )
                if (state.esAdminGeneral) {
                    StatCard(
                        numero = state.turnosCatalogo.toString(),
                        etiqueta = "TURNOS EN CATÁLOGO",
                        subetiqueta = "Plantillas activas",
                        modifier = Modifier.width(statCardWidth),
                        onClick = { onNavigate("turnos") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Banner Malla Semanal Dinámica y Ronda del Día
            if (isCompact) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ActionBanner(
                        semanaTexto = state.semanaEnCurso,
                        estadoMallaTexto = state.estadoMalla,
                        esMallaPublicada = state.esMallaPublicada,
                        modifier = Modifier.fillMaxWidth(),
                        isCompact = isCompact,
                        onActionClick = { onNavigate("malla") }
                    )
                    DailyTaskCard(
                        tareas = state.rondaDelDia,
                        modifier = Modifier.fillMaxWidth(),
                        onNavigate = onNavigate
                    )
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ActionBanner(
                        semanaTexto = state.semanaEnCurso,
                        estadoMallaTexto = state.estadoMalla,
                        esMallaPublicada = state.esMallaPublicada,
                        modifier = Modifier.weight(0.58f),
                        isCompact = isCompact,
                        onActionClick = { onNavigate("malla") }
                    )
                    DailyTaskCard(
                        tareas = state.rondaDelDia,
                        modifier = Modifier.weight(0.42f),
                        onNavigate = onNavigate
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Resumen Operativo de Asistencia Hoy
            ResumenAsistenciaCard(
                resumen = state.asistenciaHoy,
                horaActual = state.horaActualTexto,
                isCompact = isCompact,
                onVerAsistencia = { onNavigate("asistencia") }
            )

            // Comparativa por Sucursales (Solo Administrador General)
            if (state.esAdminGeneral && state.metricasPorSucursal.isNotEmpty()) {
                Spacer(modifier = Modifier.height(28.dp))
                SucursalesComparativaCard(
                    metricas = state.metricasPorSucursal,
                    sucursalSeleccionada = state.sucursalSeleccionada,
                    isCompact = isCompact,
                    onSeleccionarSucursal = { viewModel.cambiarFiltroSucursal(it) }
                )
            }
        }
    }
}

@Composable
fun GreetingHeader(
    saludo: String,
    nombre: String,
    rol: String,
    scope: String,
    fechaHoy: String,
    horaActual: String,
    isCompact: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                saludo.uppercase(),
                color = CafeTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier.size(8.dp).background(Color(0xFF16A34A), shape = CircleShape)
                )
                Text(
                    text = "TIEMPO REAL · $horaActual",
                    color = Color(0xFF16A34A),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            nombre,
            fontSize = if (isCompact) 26.sp else 32.sp,
            fontWeight = FontWeight.Bold,
            color = CafeTextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier.background(CafeDarkBrown, RoundedCornerShape(2.dp)).padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    rol.uppercase(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Text(
                scope.uppercase(),
                color = CafeTextSecondary,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp
            )

            if (fechaHoy.isNotBlank()) {
                Text(
                    "· $fechaHoy",
                    color = CafeTextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun StatCard(
    numero: String,
    etiqueta: String,
    subetiqueta: String? = null,
    badge: String? = null,
    badgeColor: Color? = null,
    colorNumero: Color = Color(0xFFA04E35),
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, CafeBorderColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    numero,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorNumero
                )
                if (badge != null) {
                    Box(
                        modifier = Modifier
                            .background((badgeColor ?: Color(0xFF16A34A)).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            badge,
                            color = badgeColor ?: Color(0xFF16A34A),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Column {
                Text(
                    etiqueta,
                    fontSize = 10.sp,
                    color = CafeTextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                if (subetiqueta != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        subetiqueta,
                        fontSize = 11.sp,
                        color = CafeTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun ActionBanner(
    semanaTexto: String,
    estadoMallaTexto: String,
    esMallaPublicada: Boolean,
    modifier: Modifier,
    isCompact: Boolean = false,
    onActionClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(Color(0xFFEBE6E0), RoundedCornerShape(4.dp))
            .border(1.dp, CafeBorderColor, RoundedCornerShape(4.dp))
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        semanaTexto,
                        color = CafeTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Box(
                        modifier = Modifier
                            .background(
                                if (esMallaPublicada) CafeStatusSuccessBg else CafeStatusWarningBg,
                                RoundedCornerShape(2.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            if (esMallaPublicada) "PUBLICADA" else "EN BORRADOR",
                            color = if (esMallaPublicada) CafeStatusSuccessText else CafeDarkBrown,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    estadoMallaTexto,
                    fontSize = if (isCompact) 20.sp else 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeTextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    if (esMallaPublicada) {
                        "El horario de la semana está publicado y activo para los empleados. Cualquier ajuste posterior quedará registrado automáticamente."
                    } else {
                        "Verifica que la franja 06:00 - 22:00 quede cubierta y los descansos legales asignados antes de publicar el horario semanal."
                    },
                    fontSize = 13.sp,
                    color = CafeTextSecondary,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            PrimarySolidButton(
                text = "Abrir malla semanal ->",
                onClick = onActionClick,
                modifier = if (isCompact) Modifier.fillMaxWidth() else Modifier.width(240.dp)
            )
        }
    }
}

@Composable
fun DailyTaskCard(
    tareas: List<TareaRonda>,
    modifier: Modifier,
    onNavigate: (String) -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, CafeBorderColor)
    ) {
        Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "RONDA DEL DÍA · ACCIONES PRIORITARIAS",
                    color = CafeTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    "${tareas.size} ÍTEMS",
                    color = CafeDarkBrown,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (tareas.isEmpty()) {
                Text("No hay tareas pendientes en este momento.", color = CafeTextSecondary, fontSize = 13.sp)
            } else {
                tareas.forEach { tarea ->
                    ItemTareaRonda(
                        tarea = tarea,
                        onClick = { onNavigate(tarea.ruta) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ItemTareaRonda(tarea: TareaRonda, onClick: () -> Unit) {
    val (dotColor, badgeBg, badgeText) = when (tarea.tipo) {
        "URGENTE" -> Triple(Color(0xFFDC2626), CafeStatusErrorBg, CafeStatusErrorText)
        "PENDIENTE" -> Triple(CafeAccentYellow, CafeStatusWarningBg, CafeDarkBrown)
        else -> Triple(Color(0xFF16A34A), CafeStatusSuccessBg, CafeStatusSuccessText)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CafeBackgroundLight, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(8.dp).background(dotColor, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                tarea.texto,
                fontSize = 12.sp,
                color = CafeTextPrimary,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .background(badgeBg, RoundedCornerShape(2.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                tarea.tipo,
                color = badgeText,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun ResumenAsistenciaCard(
    resumen: ResumenAsistenciaHoy,
    horaActual: String,
    isCompact: Boolean,
    onVerAsistencia: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, CafeBorderColor)
    ) {
        Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "OPERACIÓN DE ASISTENCIA EN TIEMPO REAL",
                        color = CafeDarkBrown,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Monitoreo de asistencia y puntualidad de la jornada ($horaActual)",
                        color = CafeTextSecondary,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clickable { onVerAsistencia() }
                        .background(CafeBackgroundLight, RoundedCornerShape(4.dp))
                        .border(1.dp, CafeBorderColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Ver control de asistencia", color = CafeDarkBrown, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = CafeDarkBrown, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Indicadores de Asistencia
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IndicadorAsistenciaMini(
                    modifier = Modifier.weight(1f),
                    valor = resumen.turnosProgramados.toString(),
                    etiqueta = "PROGRAMADOS HOY",
                    color = CafeTextPrimary
                )
                IndicadorAsistenciaMini(
                    modifier = Modifier.weight(1f),
                    valor = resumen.enTurnoAhora.toString(),
                    etiqueta = "EN TURNO AHORA",
                    color = Color(0xFF16A34A)
                )
                IndicadorAsistenciaMini(
                    modifier = Modifier.weight(1f),
                    valor = resumen.aTiempo.toString(),
                    etiqueta = "A TIEMPO",
                    color = Color(0xFF2563EB)
                )
                IndicadorAsistenciaMini(
                    modifier = Modifier.weight(1f),
                    valor = resumen.conRetraso.toString(),
                    etiqueta = "CON RETRASO",
                    color = if (resumen.conRetraso > 0) Color(0xFFDC2626) else CafeTextSecondary
                )
                IndicadorAsistenciaMini(
                    modifier = Modifier.weight(1f),
                    valor = resumen.novedadesHoy.toString(),
                    etiqueta = "NOVEDADES HOY",
                    color = if (resumen.novedadesHoy > 0) Color(0xFFB45309) else CafeTextSecondary
                )
            }
        }
    }
}

@Composable
fun IndicadorAsistenciaMini(
    valor: String,
    etiqueta: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(CafeBackgroundLight, RoundedCornerShape(4.dp))
            .border(1.dp, CafeBorderColor.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
            .padding(horizontal = 10.dp, vertical = 12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(valor, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                etiqueta,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = CafeTextSecondary,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun SucursalesComparativaCard(
    metricas: List<SucursalMetrica>,
    sucursalSeleccionada: String,
    isCompact: Boolean,
    onSeleccionarSucursal: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, CafeBorderColor)
    ) {
        Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "DESEMPEÑO Y COBERTURA POR SUCURSAL",
                    color = CafeDarkBrown,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Haz clic para filtrar",
                    color = CafeTextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isCompact) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    metricas.forEach { suc ->
                        SucursalFilaComparativa(
                            suc = suc,
                            isSelected = sucursalSeleccionada.equals(suc.nombre, ignoreCase = true),
                            onClick = { onSeleccionarSucursal(suc.nombre) }
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    metricas.forEach { suc ->
                        Box(modifier = Modifier.weight(1f)) {
                            SucursalFilaComparativa(
                                suc = suc,
                                isSelected = sucursalSeleccionada.equals(suc.nombre, ignoreCase = true),
                                onClick = { onSeleccionarSucursal(suc.nombre) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SucursalFilaComparativa(
    suc: SucursalMetrica,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val esMallaPub = suc.estadoMalla.equals("PUBLICADA", ignoreCase = true)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) CafeDarkBrown.copy(alpha = 0.05f) else Color.White,
                RoundedCornerShape(4.dp)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) CafeDarkBrown else CafeBorderColor,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SucursalChip(sucursal = suc.nombre)

                Box(
                    modifier = Modifier
                        .background(if (esMallaPub) CafeStatusSuccessBg else CafeStatusWarningBg, RoundedCornerShape(2.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        suc.estadoMalla,
                        color = if (esMallaPub) CafeStatusSuccessText else CafeDarkBrown,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Empleados", color = CafeTextSecondary, fontSize = 10.sp)
                    Text("${suc.empleadosActivos} activos", color = CafeTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Turnos hoy", color = CafeTextSecondary, fontSize = 10.sp)
                    Text("${suc.enTurnoAhora} / ${suc.turnosHoy}", color = CafeTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Novedades", color = CafeTextSecondary, fontSize = 10.sp)
                    Text(
                        "${suc.novedadesPendientes} pend.",
                        color = if (suc.novedadesPendientes > 0) Color(0xFFB45309) else CafeTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
