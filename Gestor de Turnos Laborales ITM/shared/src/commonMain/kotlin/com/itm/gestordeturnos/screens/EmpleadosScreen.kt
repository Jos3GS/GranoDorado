package com.itm.gestordeturnos.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.CalendarLocale
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import kotlin.time.Clock
import kotlinx.datetime.todayIn
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itm.gestordeturnos.CafeAccentYellow
import com.itm.gestordeturnos.CafeBackgroundLight
import com.itm.gestordeturnos.CafeBorderColor
import com.itm.gestordeturnos.CafeBorderLight
import com.itm.gestordeturnos.CafeDarkBrown
import com.itm.gestordeturnos.CafeStatusErrorBg
import com.itm.gestordeturnos.CafeStatusErrorText
import com.itm.gestordeturnos.CafeStatusSuccessBg
import com.itm.gestordeturnos.CafeStatusSuccessText
import com.itm.gestordeturnos.CafeTextPrimary
import com.itm.gestordeturnos.CafeTextSecondary
import com.itm.gestordeturnos.components.AppDropdown
import com.itm.gestordeturnos.components.EncabezadoPantalla
import com.itm.gestordeturnos.domain.model.Cargo
import com.itm.gestordeturnos.domain.model.Empleado
import com.itm.gestordeturnos.domain.model.EstadoUsuario
import com.itm.gestordeturnos.viewmodel.EmpleadosViewModel

// Constantes estáticas para evitar realocaciones innecesarias durante la recomposición
private val OPCIONES_ROLES_FILTRO = listOf("Todos", "ADMINISTRADOR GENERAL", "ADMINISTRADOR DE SUCURSAL", "BARISTA", "COCINERO", "CAJERO")
private val OPCIONES_SUCURSALES_FILTRO = listOf("Todas", "CENTRO", "NORTE", "SUR")
private val OPCIONES_ESTADOS_FILTRO = listOf("Todos", "ACTIVO", "INACTIVO")

private val ROLES_FORMULARIO = listOf("ADMINISTRADOR GENERAL", "ADMINISTRADOR DE SUCURSAL", "BARISTA", "COCINERO", "CAJERO")
private val SUCURSALES_FORMULARIO = listOf("CENTRO", "NORTE", "SUR")
private val ESTADOS_FORMULARIO = listOf("ACTIVO", "INACTIVO")

@Composable
fun EmpleadosScreen(viewModel: EmpleadosViewModel) {
    val state by viewModel.uiState.collectAsState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(CafeBackgroundLight)) {
        val isCompact = maxWidth < 950.dp
        val anchoMaximo = maxWidth

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isCompact) 16.dp else 32.dp)
            ) {
                // Encabezado unificado con EncabezadoPantalla
                EncabezadoPantalla(
                    titulo = "Empleados",
                    subtitulo = "La fuente única de verdad sobre quién trabaja, en qué sucursal y con qué rol.",
                    isCompact = isCompact,
                    accion = {
                        Button(
                            onClick = { viewModel.abrirCrearEmpleado() },
                            colors = ButtonDefaults.buttonColors(containerColor = CafeDarkBrown),
                            shape = RoundedCornerShape(2.dp),
                            modifier = if (isCompact) Modifier.fillMaxWidth() else Modifier.height(44.dp)
                        ) {
                            if (isCompact) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("+ Nuevo empleado", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Mensaje de éxito si existe
                if (state.mensajeExito != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CafeStatusSuccessBg, RoundedCornerShape(2.dp))
                            .border(1.dp, Color(0xFFC3E6CB), RoundedCornerShape(2.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = state.mensajeExito!!,
                                color = CafeStatusSuccessText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "✕",
                                color = CafeStatusSuccessText,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .clickable { viewModel.limpiarMensajeExito() }
                                    .padding(4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Fila de Filtros y Búsqueda
                BarraFiltros(
                    textoBusqueda = state.textoBusqueda,
                    onBuscarTextoCambiado = viewModel::onBuscarTextoCambiado,
                    filtroRol = state.filtroRol,
                    onFiltroRolCambiado = viewModel::onFiltroRolCambiado,
                    filtroSucursal = state.filtroSucursal,
                    onFiltroSucursalCambiado = viewModel::onFiltroSucursalCambiado,
                    filtroEstado = state.filtroEstado,
                    onFiltroEstadoCambiado = viewModel::onFiltroEstadoCambiado,
                    isCompact = isCompact
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Vista según ancho de pantalla: Tabla para escritorio, Tarjetas para móvil
                if (isCompact) {
                    ListaTarjetasEmpleados(
                        empleados = state.empleadosFiltrados,
                        onEditarClick = viewModel::abrirEditarEmpleado
                    )
                } else {
                    TablaEmpleados(
                        empleados = state.empleadosFiltrados,
                        onEditarClick = viewModel::abrirEditarEmpleado
                    )
                }
            }

            // Drawer lateral para Crear / Editar Empleado
            if (state.mostrarDrawerFormulario && state.empleadoEnEdicion != null) {
                // Fondo oscurecido
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { viewModel.cerrarFormulario() }
                )

                // Panel del Formulario
                val drawerWidth = if (anchoMaximo < 500.dp) anchoMaximo else 480.dp
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(drawerWidth)
                        .align(Alignment.CenterEnd)
                        .background(Color.White)
                        .clickable(enabled = false) {}
                ) {
                    FormularioEmpleadoDrawer(
                        empleado = state.empleadoEnEdicion!!,
                        esNuevo = state.esNuevoEmpleado,
                        isGuardando = state.isGuardando,
                        mensajeError = state.mensajeError,
                        onGuardar = viewModel::guardarEmpleado,
                        onCancelar = viewModel::cerrarFormulario
                    )
                }
            }
        }
    }
}

@Composable
private fun BarraFiltros(
    textoBusqueda: String,
    onBuscarTextoCambiado: (String) -> Unit,
    filtroRol: String,
    onFiltroRolCambiado: (String) -> Unit,
    filtroSucursal: String,
    onFiltroSucursalCambiado: (String) -> Unit,
    filtroEstado: String,
    onFiltroEstadoCambiado: (String) -> Unit,
    isCompact: Boolean
) {
    if (isCompact) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CampoBusqueda(
                texto = textoBusqueda,
                onTextoCambiado = onBuscarTextoCambiado,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    AppDropdown(
                        valorActual = filtroRol,
                        opciones = OPCIONES_ROLES_FILTRO,
                        onSeleccion = onFiltroRolCambiado,
                        label = "ROL"
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    AppDropdown(
                        valorActual = filtroSucursal,
                        opciones = OPCIONES_SUCURSALES_FILTRO,
                        onSeleccion = onFiltroSucursalCambiado,
                        label = "SUCURSAL"
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    AppDropdown(
                        valorActual = filtroEstado,
                        opciones = OPCIONES_ESTADOS_FILTRO,
                        onSeleccion = onFiltroEstadoCambiado,
                        label = "ESTADO"
                    )
                }
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CampoBusqueda(
                texto = textoBusqueda,
                onTextoCambiado = onBuscarTextoCambiado,
                modifier = Modifier.weight(1.5f)
            )
            Box(modifier = Modifier.width(170.dp)) {
                AppDropdown(
                    valorActual = filtroRol,
                    opciones = OPCIONES_ROLES_FILTRO,
                    onSeleccion = onFiltroRolCambiado,
                    label = "ROL"
                )
            }
            Box(modifier = Modifier.width(140.dp)) {
                AppDropdown(
                    valorActual = filtroSucursal,
                    opciones = OPCIONES_SUCURSALES_FILTRO,
                    onSeleccion = onFiltroSucursalCambiado,
                    label = "SUCURSAL"
                )
            }
            Box(modifier = Modifier.width(130.dp)) {
                AppDropdown(
                    valorActual = filtroEstado,
                    opciones = OPCIONES_ESTADOS_FILTRO,
                    onSeleccion = onFiltroEstadoCambiado,
                    label = "ESTADO"
                )
            }
        }
    }
}

@Composable
private fun CampoBusqueda(
    texto: String,
    onTextoCambiado: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = texto,
        onValueChange = onTextoCambiado,
        modifier = modifier
            .height(42.dp)
            .background(Color.White, RoundedCornerShape(2.dp))
            .border(1.dp, Color(0xFFD9D0C5), RoundedCornerShape(2.dp))
            .padding(horizontal = 14.dp),
        textStyle = TextStyle(fontSize = 13.sp, color = CafeTextPrimary),
        singleLine = true,
        cursorBrush = SolidColor(CafeDarkBrown),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (texto.isEmpty()) {
                    Text(
                        text = "Buscar por nombre, documento o correo...",
                        fontSize = 13.sp,
                        color = CafeTextSecondary
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun TablaEmpleados(
    empleados: List<Empleado>,
    onEditarClick: (Empleado) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(2.dp),
        border = BorderStroke(1.dp, Color(0xFFE8DFD5))
    ) {
        Column {
            // Encabezado de la Tabla
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFAF6F0))
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("COLABORADOR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CafeTextSecondary, letterSpacing = 1.sp, modifier = Modifier.weight(2.5f))
                Text("DOCUMENTO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CafeTextSecondary, letterSpacing = 1.sp, modifier = Modifier.weight(1.3f))
                Text("ROL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CafeTextSecondary, letterSpacing = 1.sp, modifier = Modifier.weight(2.2f))
                Text("SUCURSAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CafeTextSecondary, letterSpacing = 1.sp, modifier = Modifier.weight(1.1f))
                Text("ESTADO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CafeTextSecondary, letterSpacing = 1.sp, modifier = Modifier.weight(1.1f))
                Text("ACCIONES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CafeTextSecondary, letterSpacing = 1.sp, modifier = Modifier.width(80.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
            }

            HorizontalDivider(color = Color(0xFFE8DFD5))

            if (empleados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No se encontraron empleados con los filtros seleccionados.",
                        color = CafeTextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn {
                    items(empleados, key = { it.id }) { empleado ->
                        FilaEmpleado(empleado = empleado, onEditarClick = { onEditarClick(empleado) })
                        HorizontalDivider(color = Color(0xFFF3EDE4))
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaEmpleado(
    empleado: Empleado,
    onEditarClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Colaborador (Avatar Iniciales + Nombre + Correo)
        Row(
            modifier = Modifier.weight(2.5f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(Color(0xFFEFE8DE), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = empleado.iniciales,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = CafeDarkBrown
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "${empleado.nombre} ${empleado.apellido}".trim(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CafeTextPrimary
                )
                Text(
                    text = empleado.correo,
                    fontSize = 12.sp,
                    color = CafeTextSecondary
                )
            }
        }

        // Documento
        Text(
            text = if (empleado.documento > 0L) empleado.documento.toString() else "",
            fontSize = 14.sp,
            color = CafeTextPrimary,
            modifier = Modifier.weight(1.3f)
        )

        // Badge de Rol
        Box(modifier = Modifier.weight(2.2f)) {
            BadgeRol(rol = empleado.cargo.displayName)
        }

        // Chip de Sucursal
        Box(modifier = Modifier.weight(1.1f)) {
            Box(
                modifier = Modifier
                    .border(1.dp, Color(0xFFDCC8B3), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = empleado.sucursal,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFA57D52),
                    letterSpacing = 1.sp
                )
            }
        }

        // Estado (Punto verde/rojo + Texto)
        Row(
            modifier = Modifier.weight(1.1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isActivo = empleado.estado == EstadoUsuario.ACTIVO
            val puntoColor = if (isActivo) CafeStatusSuccessText else CafeStatusErrorText
            val textoColor = if (isActivo) CafeStatusSuccessText else CafeStatusErrorText

            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(puntoColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = empleado.estado.displayName.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textoColor,
                letterSpacing = 0.5.sp
            )
        }

        // Botón EDITAR
        Box(
            modifier = Modifier.width(80.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = "EDITAR",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CafeTextSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .clickable { onEditarClick() }
                    .padding(4.dp),
                textDecoration = TextDecoration.Underline
            )
        }
    }
}

@Composable
private fun ListaTarjetasEmpleados(
    empleados: List<Empleado>,
    onEditarClick: (Empleado) -> Unit
) {
    if (empleados.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No se encontraron empleados con los filtros seleccionados.",
                color = CafeTextSecondary,
                fontSize = 14.sp
            )
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(empleados, key = { it.id }) { empleado ->
                TarjetaEmpleadoCompacta(empleado = empleado, onEditarClick = { onEditarClick(empleado) })
            }
        }
    }
}

@Composable
private fun TarjetaEmpleadoCompacta(
    empleado: Empleado,
    onEditarClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(2.dp),
        border = BorderStroke(1.dp, Color(0xFFE8DFD5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFEFE8DE), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = empleado.iniciales,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = CafeDarkBrown
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${empleado.nombre} ${empleado.apellido}".trim(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CafeTextPrimary
                        )
                        Text(
                            text = if (empleado.documento > 0L) "CC: ${empleado.documento}" else "",
                            fontSize = 11.sp,
                            color = CafeTextSecondary
                        )
                    }
                }

                Text(
                    text = "EDITAR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeTextSecondary,
                    letterSpacing = 1.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onEditarClick() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF3EDE4))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BadgeRol(rol = empleado.cargo.displayName)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .border(1.dp, Color(0xFFDCC8B3), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = empleado.sucursal,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA57D52)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))

                    val isActivo = empleado.estado == EstadoUsuario.ACTIVO
                    Text(
                        text = "• ${empleado.estado.displayName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActivo) CafeStatusSuccessText else CafeStatusErrorText
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgeRol(rol: String) {
    val (fondo, texto) = when (rol.uppercase()) {
        "ADMINISTRADOR GENERAL" -> Color(0xFF2C1E16) to Color(0xFFF7F4EF)
        "ADMINISTRADOR DE SUCURSAL" -> Color(0xFFF3DEBA) to Color(0xFF5D4037)
        "BARISTA" -> Color(0xFFE2EBD8) to Color(0xFF3C6036)
        "COCINERO" -> Color(0xFFF9DFDF) to Color(0xFF8B3A3A)
        "CAJERO" -> Color(0xFFD8EAF2) to Color(0xFF27556C)
        "SUPERVISOR" -> Color(0xFFF3DEBA) to Color(0xFF5D4037)
        else -> Color(0xFFECE7DF) to Color(0xFF4A3E39)
    }

    Box(
        modifier = Modifier
            .background(fondo, RoundedCornerShape(2.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = rol.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = texto,
            letterSpacing = 0.5.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private object UtcDatePickerFormatter : DatePickerFormatter {
    private val meses = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    override fun formatMonthYear(monthMillis: Long?, locale: CalendarLocale): String? {
        if (monthMillis == null) return null
        val instant = Instant.fromEpochMilliseconds(monthMillis)
        val ldt = instant.toLocalDateTime(TimeZone.UTC)
        val nombreMes = meses.getOrElse(ldt.monthNumber - 1) { "" }
        return "$nombreMes ${ldt.year}"
    }

    override fun formatDate(
        dateMillis: Long?,
        locale: CalendarLocale,
        forContentDescription: Boolean
    ): String? {
        if (dateMillis == null) return null
        val instant = Instant.fromEpochMilliseconds(dateMillis)
        val ldt = instant.toLocalDateTime(TimeZone.UTC)
        val dia = ldt.dayOfMonth.toString().padStart(2, '0')
        val mes = ldt.monthNumber.toString().padStart(2, '0')
        val anio = ldt.year
        val nombreMes = meses.getOrElse(ldt.monthNumber - 1) { "" }
        return if (forContentDescription) "$dia de $nombreMes de $anio" else "$dia/$mes/$anio"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormularioEmpleadoDrawer(
    empleado: Empleado,
    esNuevo: Boolean,
    isGuardando: Boolean = false,
    mensajeError: String?,
    onGuardar: (Empleado) -> Unit,
    onCancelar: () -> Unit
) {
    var mostrarDatePicker by remember { mutableStateOf(false) }
    var nombre by remember(empleado) { mutableStateOf(empleado.nombre) }
    var apellido by remember(empleado) { mutableStateOf(empleado.apellido) }
    var documento by remember(empleado) { mutableStateOf(if (empleado.documento > 0L) empleado.documento.toString() else "") }
    var correo by remember(empleado) { mutableStateOf(empleado.correo) }
    var telefono by remember(empleado) { mutableStateOf(empleado.telefono) }
    var rolSeleccionado by remember(empleado) { mutableStateOf(empleado.cargo.displayName) }
    var sucursalSeleccionada by remember(empleado) { mutableStateOf(empleado.sucursal) }
    var fechaIngreso by remember(empleado) {
        val fecha = if (empleado.fechaIngreso.isNotBlank() && (!esNuevo || empleado.fechaIngreso != "15/01/2024")) {
            empleado.fechaIngreso
        } else {
            val hoy = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val dia = hoy.dayOfMonth.toString().padStart(2, '0')
            val mes = hoy.monthNumber.toString().padStart(2, '0')
            val anio = hoy.year
            "$dia/$mes/$anio"
        }
        mutableStateOf(fecha)
    }
    var estadoAcceso by remember(empleado) { mutableStateOf(empleado.estado.displayName) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 28.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = if (esNuevo) "NUEVO EMPLEADO" else "EDITAR EMPLEADO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeTextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (esNuevo) "Registrar colaborador" else "${empleado.nombre} ${empleado.apellido}".trim().ifEmpty { "Empleado" },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeTextPrimary
                )
            }
            IconButton(onClick = onCancelar) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = CafeTextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (mensajeError != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CafeStatusErrorBg, RoundedCornerShape(2.dp))
                    .border(1.dp, Color(0xFFF5C6CB), RoundedCornerShape(2.dp))
                    .padding(12.dp)
            ) {
                Text(mensajeError, color = CafeStatusErrorText, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Nombre y Apellido
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                EtiquetaCampo("NOMBRE *")
                CampoTextoFormulario(valor = nombre, onValorCambiado = { nombre = it }, placeholder = "Ej: Carlos")
            }
            Column(modifier = Modifier.weight(1f)) {
                EtiquetaCampo("APELLIDO *")
                CampoTextoFormulario(valor = apellido, onValorCambiado = { apellido = it }, placeholder = "Ej: Ruiz")
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Documento y Teléfono
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                EtiquetaCampo("NÚMERO DE DOCUMENTO *")
                CampoTextoFormulario(
                    valor = documento,
                    onValorCambiado = { if (esNuevo) documento = it },
                    placeholder = "1020304050",
                    enabled = esNuevo
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                EtiquetaCampo("TELÉFONO")
                CampoTextoFormulario(valor = telefono, onValorCambiado = { telefono = it }, placeholder = "3001234567")
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Correo Electrónico
        EtiquetaCampo("CORREO ELECTRÓNICO *")
        CampoTextoFormulario(valor = correo, onValorCambiado = { correo = it }, placeholder = "carlos.ruiz@granodorado.com")

        Spacer(modifier = Modifier.height(18.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                EtiquetaCampo("ROL")
                DropdownFormulario(
                    valorActual = rolSeleccionado,
                    opciones = ROLES_FORMULARIO,
                    onSeleccion = { rolSeleccionado = it }
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                EtiquetaCampo("SUCURSAL")
                DropdownFormulario(
                    valorActual = sucursalSeleccionada,
                    opciones = SUCURSALES_FORMULARIO,
                    onSeleccion = { sucursalSeleccionada = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                EtiquetaCampo("FECHA DE INGRESO")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .background(Color.White, RoundedCornerShape(2.dp))
                        .border(1.dp, Color(0xFFD9D0C5), RoundedCornerShape(2.dp))
                        .clickable { mostrarDatePicker = true }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(fechaIngreso, fontSize = 13.sp, color = CafeTextPrimary)
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Seleccionar fecha de ingreso",
                            tint = CafeTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                EtiquetaCampo("ESTADO DE ACCESO")
                DropdownFormulario(
                    valorActual = estadoAcceso,
                    opciones = ESTADOS_FORMULARIO,
                    onSeleccion = { estadoAcceso = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    val docLong = if (esNuevo) {
                        documento.filter { it.isDigit() }.toLongOrNull() ?: 0L
                    } else {
                        empleado.documento
                    }
                    val cargoEnum = Cargo.entries.find {
                        it.displayName.equals(rolSeleccionado, ignoreCase = true) || it.name.equals(rolSeleccionado, ignoreCase = true)
                    } ?: Cargo.BARISTA
                    val estadoEnum = EstadoUsuario.entries.find {
                        it.displayName.equals(estadoAcceso, ignoreCase = true) || it.name.equals(estadoAcceso, ignoreCase = true)
                    } ?: EstadoUsuario.ACTIVO

                    val modificado = empleado.copy(
                        nombre = nombre.trim(),
                        apellido = apellido.trim(),
                        documento = docLong,
                        correo = correo.trim(),
                        telefono = telefono.trim(),
                        cargo = cargoEnum,
                        sucursal = sucursalSeleccionada,
                        fechaIngreso = fechaIngreso,
                        estado = estadoEnum
                    )
                    onGuardar(modificado)
                },
                enabled = !isGuardando,
                colors = ButtonDefaults.buttonColors(containerColor = CafeDarkBrown),
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier
                    .weight(1.3f)
                    .height(44.dp)
            ) {
                Text(
                    text = if (isGuardando) "Guardando..." else "Guardar",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            OutlinedButton(
                onClick = onCancelar,
                enabled = !isGuardando,
                border = BorderStroke(1.dp, Color(0xFFD9D0C5)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            ) {
                Text("Cancelar", color = CafeTextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            }
        }
    }

    if (mostrarDatePicker) {
        val initialMillis = remember(fechaIngreso) {
            try {
                val partes = fechaIngreso.split("/")
                if (partes.size == 3) {
                    val dia = partes[0].toInt()
                    val mes = partes[1].toInt()
                    val anio = partes[2].toInt()
                    kotlinx.datetime.LocalDate(anio, mes, dia)
                        .atStartOfDayIn(TimeZone.UTC)
                        .toEpochMilliseconds()
                } else {
                    val hoy = Clock.System.todayIn(TimeZone.currentSystemDefault())
                    kotlinx.datetime.LocalDate(hoy.year, hoy.monthNumber, hoy.dayOfMonth)
                        .atStartOfDayIn(TimeZone.UTC)
                        .toEpochMilliseconds()
                }
            } catch (_: Exception) {
                val hoy = Clock.System.todayIn(TimeZone.currentSystemDefault())
                kotlinx.datetime.LocalDate(hoy.year, hoy.monthNumber, hoy.dayOfMonth)
                    .atStartOfDayIn(TimeZone.UTC)
                    .toEpochMilliseconds()
            }
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis, initialDisplayedMonthMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val instant = Instant.fromEpochMilliseconds(millis)
                            val localDate = instant.toLocalDateTime(TimeZone.UTC).date
                            val dia = localDate.dayOfMonth.toString().padStart(2, '0')
                            val mes = localDate.monthNumber.toString().padStart(2, '0')
                            val anio = localDate.year
                            fechaIngreso = "$dia/$mes/$anio"
                        }
                        mostrarDatePicker = false
                    }
                ) {
                    Text("Aceptar", color = CafeDarkBrown, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) {
                    Text("Cancelar", color = CafeTextSecondary)
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                dateFormatter = remember { UtcDatePickerFormatter },
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = CafeDarkBrown,
                    selectedDayContentColor = Color.White,
                    todayDateBorderColor = CafeDarkBrown,
                    todayContentColor = CafeDarkBrown
                )
            )
        }
    }
}

@Composable
private fun EtiquetaCampo(texto: String) {
    Text(
        text = texto,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = CafeTextSecondary,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun CampoTextoFormulario(
    valor: String,
    onValorCambiado: (String) -> Unit,
    placeholder: String,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambiado,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(fontSize = 13.sp, color = if (enabled) CafeTextPrimary else CafeTextSecondary),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CafeDarkBrown,
            unfocusedBorderColor = Color(0xFFD9D0C5),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color(0xFFF7F4F0),
            disabledBorderColor = Color(0xFFE2DCD5),
            disabledTextColor = CafeTextSecondary
        ),
        shape = RoundedCornerShape(2.dp),
        placeholder = { Text(placeholder, fontSize = 13.sp, color = Color(0xFFAFA79D)) }
    )
}

@Composable
private fun DropdownFormulario(
    valorActual: String,
    opciones: List<String>,
    onSeleccion: (String) -> Unit
) {
    AppDropdown(
        valorActual = valorActual,
        opciones = opciones,
        onSeleccion = onSeleccion,
        label = ""
    )
}
