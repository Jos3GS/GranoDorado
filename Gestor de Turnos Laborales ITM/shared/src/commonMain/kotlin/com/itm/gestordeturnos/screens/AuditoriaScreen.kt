package com.itm.gestordeturnos.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.itm.gestordeturnos.CafeStatusWarningText
import com.itm.gestordeturnos.CafeTextPrimary
import com.itm.gestordeturnos.CafeTextSecondary
import com.itm.gestordeturnos.components.AppDropdown
import com.itm.gestordeturnos.components.EncabezadoPantalla
import com.itm.gestordeturnos.components.FiltroDropdown
import com.itm.gestordeturnos.components.SucursalChip
import com.itm.gestordeturnos.domain.model.EventoAuditoria
import com.itm.gestordeturnos.domain.model.TipoAccionAuditoria
import com.itm.gestordeturnos.viewmodel.AuditoriaViewModel

@Composable
fun AuditoriaScreen(viewModel: AuditoriaViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val opcionesAccion = remember {
        listOf("TODAS") + TipoAccionAuditoria.entries.map { it.name }
    }
    val seleccionAccionTexto = state.filtroTipoAccion?.name ?: "TODAS"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CafeBackgroundLight)
            .padding(24.dp)
    ) {
        EncabezadoPantalla(
            titulo = "Auditoría y Trazabilidad",
            subtitulo = "Historial inmutable de eventos críticos, accesos y modificaciones del sistema"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // BARRA DE FILTROS Y BÚSQUEDA
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, CafeBorderLight),
            shape = RoundedCornerShape(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Filtro Sucursal
                    FiltroDropdown(
                        opcionSeleccionada = state.filtroSucursal,
                        opciones = state.sucursalesDisponibles,
                        onOpcionSeleccionada = { viewModel.cambiarFiltroSucursal(it) },
                        modifier = Modifier.width(160.dp)
                    )

                    // Filtro Tipo de Acción
                    Box(modifier = Modifier.width(220.dp)) {
                        AppDropdown(
                            valorActual = seleccionAccionTexto,
                            opciones = opcionesAccion,
                            onSeleccion = { seleccion ->
                                val tipo = if (seleccion == "TODAS") null
                                           else TipoAccionAuditoria.entries.find { it.name == seleccion }
                                viewModel.cambiarFiltroTipo(tipo)
                            }
                        )
                    }

                    // Campo de búsqueda libre
                    OutlinedTextField(
                        value = state.filtroTexto,
                        onValueChange = { viewModel.cambiarFiltroTexto(it) },
                        placeholder = { Text("Buscar por usuario, entidad o detalle...", fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(2.dp)
                    )

                    // Botón limpiar filtros
                    if (state.filtroTipoAccion != null || state.filtroSucursal != "TODAS" || state.filtroTexto.isNotBlank() || state.filtroFecha.isNotBlank()) {
                        OutlinedButton(
                            onClick = { viewModel.limpiarFiltros() },
                            border = BorderStroke(1.dp, CafeBorderColor),
                            shape = RoundedCornerShape(2.dp)
                        ) {
                            Text("Limpiar", color = CafeTextPrimary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // RESUMEN Y CONTADOR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mostrando ${state.eventosFiltrados.size} de ${state.eventosOriginales.size} eventos registrados",
                color = CafeTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // LISTA DE EVENTOS DE AUDITORÍA
        if (state.eventosFiltrados.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, CafeBorderLight),
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No se encontraron eventos de auditoría con los criterios de búsqueda aplicados.",
                        color = CafeTextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.eventosFiltrados, key = { it.id }) { evento ->
                    TarjetaEventoAuditoria(evento = evento)
                }
            }
        }
    }
}

@Composable
private fun TarjetaEventoAuditoria(evento: EventoAuditoria) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CafeBorderLight),
        shape = RoundedCornerShape(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Encabezado del Evento: Tipo de Acción, Fecha/Hora y Sucursal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BadgeAccionAuditoria(tipoAccion = evento.tipoAccion)
                    Text(
                        text = evento.entidad.ifBlank { "SISTEMA" },
                        color = CafeTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (evento.entidadId.isNotBlank()) {
                        Text(
                            text = "#${evento.entidadId}",
                            color = CafeTextSecondary.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SucursalChip(sucursal = evento.sucursal.ifBlank { "CENTRO" })
                    Text(
                        text = evento.fechaHoraIso,
                        color = CafeTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Usuario responsable
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Responsable: ",
                    color = CafeTextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = "${evento.usuarioNombre} (${evento.rol.ifBlank { "USUARIO" }})",
                    color = CafeTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (evento.detalles.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = evento.detalles,
                    color = CafeTextPrimary,
                    fontSize = 13.sp
                )
            }

            // Comparativa de estado previo vs estado nuevo (si aplica)
            if (evento.valorAnterior.isNotBlank() || evento.valorNuevo.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (evento.valorAnterior.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFFF9F9F9), RoundedCornerShape(2.dp))
                                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(2.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(
                                    text = "VALOR ANTERIOR",
                                    color = CafeTextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = evento.valorAnterior,
                                    color = CafeTextPrimary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    if (evento.valorNuevo.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFFF5F9F6), RoundedCornerShape(2.dp))
                                .border(1.dp, Color(0xFFD4EADB), RoundedCornerShape(2.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(
                                    text = "VALOR NUEVO",
                                    color = Color(0xFF2E7D32),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = evento.valorNuevo,
                                    color = CafeTextPrimary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeAccionAuditoria(tipoAccion: TipoAccionAuditoria) {
    val (bg, textoColor, label) = when (tipoAccion) {
        TipoAccionAuditoria.LOGIN_EXITOSO -> Triple(CafeStatusSuccessBg, CafeStatusSuccessText, "LOGIN EXITOSO")
        TipoAccionAuditoria.LOGIN_FALLIDO -> Triple(CafeStatusErrorBg, CafeStatusErrorText, "LOGIN FALLIDO")
        TipoAccionAuditoria.CREAR_EMPLEADO -> Triple(CafeStatusInfoBg, CafeStatusInfoText, "CREAR EMPLEADO")
        TipoAccionAuditoria.MODIFICAR_EMPLEADO -> Triple(CafeStatusWarningBg, CafeStatusWarningText, "MODIFICAR EMPLEADO")
        TipoAccionAuditoria.CREAR_TURNO -> Triple(CafeStatusInfoBg, CafeStatusInfoText, "CREAR TURNO")
        TipoAccionAuditoria.MODIFICAR_TURNO -> Triple(CafeStatusWarningBg, CafeStatusWarningText, "MODIFICAR TURNO")
        TipoAccionAuditoria.PUBLICAR_MALLA -> Triple(Color(0xFFEDE7F6), Color(0xFF5E35B1), "PUBLICAR MALLA")
        TipoAccionAuditoria.APROBAR_CAMBIO_TURNO -> Triple(CafeStatusSuccessBg, CafeStatusSuccessText, "APROBAR CAMBIO")
        TipoAccionAuditoria.RECHAZAR_CAMBIO_TURNO -> Triple(CafeStatusErrorBg, CafeStatusErrorText, "RECHAZAR CAMBIO")
        TipoAccionAuditoria.CANCELAR_CAMBIO_TURNO -> Triple(Color(0xFFEEEEEE), CafeTextSecondary, "CANCELAR CAMBIO")
        TipoAccionAuditoria.MARCA_SUPERVISOR -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "MARCA MANUAL SUPERVISOR")
        TipoAccionAuditoria.REGISTRAR_NOVEDAD -> Triple(Color(0xFFE0F2F1), Color(0xFF00695C), "REGISTRAR NOVEDAD")
        TipoAccionAuditoria.REVISAR_NOVEDAD -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "REVISAR NOVEDAD")
    }

    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(2.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = textoColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
