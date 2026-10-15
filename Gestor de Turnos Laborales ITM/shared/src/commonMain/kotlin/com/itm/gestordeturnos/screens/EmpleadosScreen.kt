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
import androidx.compose.material3.Text
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
import com.itm.gestordeturnos.domain.model.Empleado
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

                Spacer(modifier = Modifier.height(16.dp))

                // Barra de filtros: Buscador + Dropdowns con título ENCIMA
                if (isCompact) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "BÚSQUEDA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CafeTextSecondary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FiltroBuscadorInput(
                                valor = state.textoBusqueda,
                                onValorCambio = { viewModel.onBuscarTextoCambiado(it) },
                                modifier = Modifier.fillMaxWidth().height(44.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FiltroSelectorCompacto(
                                label = "Rol",
                                valorActual = state.filtroRol,
                                opciones = OPCIONES_ROLES_FILTRO,
                                onSeleccion = { viewModel.onFiltroRolCambiado(it) },
                                modifier = Modifier.weight(1f)
                            )
                            FiltroSelectorCompacto(
                                label = "Sucursal",
                                valorActual = state.filtroSucursal,
                                opciones = OPCIONES_SUCURSALES_FILTRO,
                                onSeleccion = { viewModel.onFiltroSucursalCambiado(it) },
                                modifier = Modifier.weight(1f)
                            )
                            FiltroSelectorCompacto(
                                label = "Estado",
                                valorActual = state.filtroEstado,
                                opciones = OPCIONES_ESTADOS_FILTRO,
                                onSeleccion = { viewModel.onFiltroEstadoCambiado(it) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "BÚSQUEDA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CafeTextSecondary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FiltroBuscadorInput(
                                valor = state.textoBusqueda,
                                onValorCambio = { viewModel.onBuscarTextoCambiado(it) },
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        FiltroSelector(
                            label = "Rol",
                            valorActual = state.filtroRol,
                            opciones = OPCIONES_ROLES_FILTRO,
                            onSeleccion = { viewModel.onFiltroRolCambiado(it) },
                            modifier = Modifier.width(170.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        FiltroSelector(
                            label = "Sucursal",
                            valorActual = state.filtroSucursal,
                            opciones = OPCIONES_SUCURSALES_FILTRO,
                            onSeleccion = { viewModel.onFiltroSucursalCambiado(it) },
                            modifier = Modifier.width(150.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        FiltroSelector(
                            label = "Estado",
                            valorActual = state.filtroEstado,
                            opciones = OPCIONES_ESTADOS_FILTRO,
                            onSeleccion = { viewModel.onFiltroEstadoCambiado(it) },
                            modifier = Modifier.width(140.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Tabla / Lista de Empleados
                if (isCompact) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.empleadosFiltrados, key = { it.id }) { empleado ->
                            EmpleadoCardMovil(
                                empleado = empleado,
                                onEditarClick = { viewModel.abrirEditarEmpleado(empleado) }
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(1.dp, Color(0xFFEFE9E1), RoundedCornerShape(2.dp))
                            .background(Color.White)
                    ) {
                        // Cabecera de la tabla
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFCFAF7))
                                .border(1.dp, Color(0xFFEFE9E1))
                                .padding(horizontal = 24.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("EMPLEADO", color = CafeTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.weight(2.5f))
                            Text("DOCUMENTO", color = CafeTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.weight(1.3f))
                            Text("ROL", color = CafeTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.weight(2.2f))
                            Text("SUCURSAL", color = CafeTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.weight(1.1f))
                            Text("ESTADO", color = CafeTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.weight(1.1f))
                            Text("", modifier = Modifier.width(80.dp))
                        }

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.empleadosFiltrados, key = { it.id }) { empleado ->
                                EmpleadoFilaTabla(
                                    empleado = empleado,
                                    onEditarClick = { viewModel.abrirEditarEmpleado(empleado) }
                                )
                                HorizontalDivider(color = Color(0xFFF3EDE4), thickness = 1.dp)
                            }
                        }
                    }
                }
            }

            // Panel Lateral / Drawer del Formulario (Crear / Editar Empleado)
            if (state.mostrarDrawerFormulario && state.empleadoEnEdicion != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                        .clickable { viewModel.cerrarFormulario() }
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(if (isCompact) anchoMaximo else 450.dp)
                        .background(Color(0xFFFAF7F2))
                        .clickable(enabled = false) {}
                ) {
                    FormularioEmpleadoDrawer(
                        empleado = state.empleadoEnEdicion!!,
                        esNuevo = state.esNuevoEmpleado,
                        mensajeError = state.mensajeError,
                        onGuardar = { viewModel.guardarEmpleado(it) },
                        onCancelar = { viewModel.cerrarFormulario() }
                    )
                }
            }
        }
    }
}

@Composable
private fun FiltroBuscadorInput(
    valor: String,
    onValorCambio: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(2.dp))
            .border(1.dp, CafeBorderColor, RoundedCornerShape(2.dp))
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = valor,
            onValueChange = onValorCambio,
            singleLine = true,
            textStyle = TextStyle(
                color = CafeTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal
            ),
            cursorBrush = SolidColor(CafeDarkBrown),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (valor.isEmpty()) {
                        Text(
                            text = "Buscar por nombre, documento o correo...",
                            fontSize = 13.sp,
                            color = CafeTextSecondary.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun EmpleadoFilaTabla(
    empleado: Empleado,
    onEditarClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar + Nombre + Correo
        Row(
            modifier = Modifier.weight(2.5f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0xFFF5E6D3), RoundedCornerShape(2.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = empleado.iniciales,
                    fontWeight = FontWeight.Bold,
                    color = CafeDarkBrown,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "${empleado.nombre} ${empleado.apellido}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = CafeTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = empleado.correo,
                    fontSize = 12.sp,
                    color = CafeTextSecondary
                )
            }
        }

        // Documento
        Text(
            text = empleado.documento,
            fontSize = 14.sp,
            color = CafeTextPrimary,
            modifier = Modifier.weight(1.3f)
        )

        // Badge de Rol
        Box(modifier = Modifier.weight(2.2f)) {
            BadgeRol(rol = empleado.cargo)
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
            val isActivo = empleado.estado.equals("ACTIVO", ignoreCase = true)
            val puntoColor = if (isActivo) CafeStatusSuccessText else CafeStatusErrorText
            val textoColor = if (isActivo) CafeStatusSuccessText else CafeStatusErrorText

            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(puntoColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = empleado.estado.uppercase(),
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
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable { onEditarClick() }
                    .padding(vertical = 4.dp, horizontal = 6.dp)
            )
        }
    }
}

@Composable
private fun EmpleadoCardMovil(
    empleado: Empleado,
    onEditarClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CafeBorderLight),
        shape = RoundedCornerShape(4.dp)
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
                            .size(38.dp)
                            .background(Color(0xFFF5E6D3), RoundedCornerShape(2.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = empleado.iniciales,
                            fontWeight = FontWeight.Bold,
                            color = CafeDarkBrown,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${empleado.nombre} ${empleado.apellido}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = CafeTextPrimary
                        )
                        Text(
                            text = empleado.correo,
                            fontSize = 12.sp,
                            color = CafeTextSecondary
                        )
                    }
                }

                Text(
                    text = "EDITAR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeDarkBrown,
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
                BadgeRol(rol = empleado.cargo)

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

                    val isActivo = empleado.estado.equals("ACTIVO", ignoreCase = true)
                    Text(
                        text = "• ${empleado.estado}",
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
        else -> Color(0xFFEEEEEE) to Color(0xFF555555)
    }

    Box(
        modifier = Modifier
            .background(fondo, RoundedCornerShape(2.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = rol.uppercase(),
            color = texto,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FiltroSelector(
    label: String,
    valorActual: String,
    opciones: List<String>,
    onSeleccion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AppDropdown(
        label = label,
        valorActual = valorActual,
        opciones = opciones,
        onSeleccion = onSeleccion,
        modifier = modifier,
        altura = 48.dp,
        formatoItemMenu = { opcion ->
            if (opcion == "Todos" || opcion == "Todas") "$opcion ($label)" else opcion
        }
    )
}

@Composable
private fun FiltroSelectorCompacto(
    label: String,
    valorActual: String,
    opciones: List<String>,
    onSeleccion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AppDropdown(
        label = label,
        valorActual = valorActual,
        opciones = opciones,
        onSeleccion = onSeleccion,
        modifier = modifier,
        altura = 44.dp,
        isCompact = true,
        formatoItemMenu = { opcion ->
            if (opcion == "Todos" || opcion == "Todas") "$opcion ($label)" else opcion
        }
    )
}

@Composable
private fun FormularioEmpleadoDrawer(
    empleado: Empleado,
    esNuevo: Boolean,
    mensajeError: String?,
    onGuardar: (Empleado) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre by remember(empleado) { mutableStateOf(empleado.nombre) }
    var apellido by remember(empleado) { mutableStateOf(empleado.apellido) }
    var documento by remember(empleado) { mutableStateOf(empleado.documento) }
    var correo by remember(empleado) { mutableStateOf(empleado.correo) }
    var telefono by remember(empleado) { mutableStateOf(empleado.telefono) }
    var rolSeleccionado by remember(empleado) { mutableStateOf(empleado.cargo) }
    var sucursalSeleccionada by remember(empleado) { mutableStateOf(empleado.sucursal) }
    var fechaIngreso by remember(empleado) { mutableStateOf(empleado.fechaIngreso) }
    var estadoAcceso by remember(empleado) { mutableStateOf(empleado.estado) }

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
                    .border(1.dp, Color(0xFFF8B4B4), RoundedCornerShape(2.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = mensajeError,
                    color = CafeStatusErrorText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                EtiquetaCampo("NOMBRE")
                CampoTextoFormulario(valor = nombre, onValorCambio = { nombre = it }, placeholder = "Ej: Diego")
            }
            Column(modifier = Modifier.weight(1f)) {
                EtiquetaCampo("APELLIDO")
                CampoTextoFormulario(valor = apellido, onValorCambio = { apellido = it }, placeholder = "Ej: Marín")
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        EtiquetaCampo("NÚMERO DE DOCUMENTO")
        CampoTextoFormulario(
            valor = documento,
            onValorCambio = { documento = it },
            placeholder = "Ej: 1000112233",
            habilitado = esNuevo
        )
        if (!esNuevo) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "No editable: identifica al empleado.",
                fontSize = 11.sp,
                color = CafeTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        EtiquetaCampo("CORREO ELECTRÓNICO")
        CampoTextoFormulario(
            valor = correo,
            onValorCambio = { correo = it },
            placeholder = "nombre.apellido@granodorado.com"
        )

        Spacer(modifier = Modifier.height(18.dp))

        EtiquetaCampo("TELÉFONO (OPCIONAL)")
        CampoTextoFormulario(
            valor = telefono,
            onValorCambio = { telefono = it },
            placeholder = "Ej: 3001112233"
        )

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
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(fechaIngreso, fontSize = 13.sp, color = CafeTextPrimary)
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = CafeTextSecondary, modifier = Modifier.size(18.dp))
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
                    val modificado = empleado.copy(
                        nombre = nombre.trim(),
                        apellido = apellido.trim(),
                        documento = documento.trim(),
                        correo = correo.trim(),
                        telefono = telefono.trim(),
                        cargo = rolSeleccionado,
                        sucursal = sucursalSeleccionada,
                        fechaIngreso = fechaIngreso,
                        estado = estadoAcceso
                    )
                    onGuardar(modificado)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CafeDarkBrown),
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier
                    .weight(1.3f)
                    .height(44.dp)
            ) {
                Text("Guardar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            OutlinedButton(
                onClick = onCancelar,
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
}

@Composable
private fun EtiquetaCampo(texto: String) {
    Text(
        text = texto,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = CafeTextSecondary,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun CampoTextoFormulario(
    valor: String,
    onValorCambio: (String) -> Unit,
    placeholder: String,
    habilitado: Boolean = true
) {
    val fondo = if (habilitado) Color.White else Color(0xFFF0EAE1)
    val borde = if (habilitado) Color(0xFFD9D0C5) else Color(0xFFE4DDD5)

    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambio,
        enabled = habilitado,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, fontSize = 13.sp, color = CafeTextSecondary.copy(alpha = 0.6f)) },
        singleLine = true,
        shape = RoundedCornerShape(2.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = fondo,
            unfocusedContainerColor = fondo,
            disabledContainerColor = fondo,
            focusedBorderColor = CafeAccentYellow,
            unfocusedBorderColor = borde,
            disabledBorderColor = borde,
            focusedTextColor = CafeTextPrimary,
            unfocusedTextColor = CafeTextPrimary,
            disabledTextColor = CafeTextSecondary
        )
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
        altura = 56.dp,
        colorBorde = CafeBorderLight
    )
}
