package com.itm.gestordeturnos.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.itm.gestordeturnos.CafeDarkBrown
import com.itm.gestordeturnos.CafeStatusErrorBg
import com.itm.gestordeturnos.CafeStatusErrorText
import com.itm.gestordeturnos.CafeStatusSuccessBg
import com.itm.gestordeturnos.CafeStatusSuccessText
import com.itm.gestordeturnos.CafeTextPrimary
import com.itm.gestordeturnos.CafeTextSecondary
import com.itm.gestordeturnos.components.LabeledInputField
import com.itm.gestordeturnos.components.PrimarySolidButton
import com.itm.gestordeturnos.domain.model.UsuarioActual
import com.itm.gestordeturnos.viewmodel.CambiarContrasenaViewModel

@Composable
fun CambiarContrasenaScreen(
    viewModel: CambiarContrasenaViewModel,
    usuario: UsuarioActual?,
    onVolver: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CafeBackgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Encabezado con botón Volver
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            IconButton(onClick = onVolver) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = CafeDarkBrown
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "SEGURIDAD DE LA CUENTA",
                    fontSize = 11.sp,
                    color = CafeTextSecondary,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Cambiar Contraseña",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeTextPrimary
                )
            }
        }

        // Tarjeta principal del formulario
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CafeBorderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Info del usuario
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CafeBackgroundLight, RoundedCornerShape(6.dp))
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(CafeAccentYellow, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = CafeDarkBrown,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = usuario?.nombre ?: "Usuario",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CafeTextPrimary
                        )
                        Text(
                            text = usuario?.correo ?: "",
                            fontSize = 13.sp,
                            color = CafeTextSecondary
                        )
                    }
                }

                // Requisitos según Firebase Auth
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF0FDF4), RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(6.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Condiciones de contraseña (Firebase Auth):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF166534)
                        )
                        Text(
                            text = "• Mínimo 6 caracteres de longitud (exigido por Firebase Authentication).",
                            fontSize = 12.sp,
                            color = Color(0xFF15803D)
                        )
                        Text(
                            text = "• Debe ser distinta a tu contraseña actual.",
                            fontSize = 12.sp,
                            color = Color(0xFF15803D)
                        )
                    }
                }

                // Campos de entrada
                LabeledInputField(
                    label = "Contraseña Actual",
                    value = state.claveActual,
                    onValueChange = viewModel::onClaveActualChanged,
                    placeholder = "Ingresa tu contraseña actual",
                    isPassword = true
                )

                LabeledInputField(
                    label = "Nueva Contraseña",
                    value = state.nuevaClave,
                    onValueChange = viewModel::onNuevaClaveChanged,
                    placeholder = "Mínimo 6 caracteres",
                    isPassword = true
                )

                LabeledInputField(
                    label = "Confirmar Nueva Contraseña",
                    value = state.confirmacionClave,
                    onValueChange = viewModel::onConfirmacionClaveChanged,
                    placeholder = "Repite la nueva contraseña",
                    isPassword = true
                )

                // Mensajes de error o éxito
                state.mensajeError?.let { error ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CafeStatusErrorBg, RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFFF5C6CB), RoundedCornerShape(4.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = error,
                            color = CafeStatusErrorText,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                state.mensajeExito?.let { exito ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CafeStatusSuccessBg, RoundedCornerShape(4.dp))
                            .border(1.dp, Color(0xFFC3E6CB), RoundedCornerShape(4.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = CafeStatusSuccessText,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = exito,
                                color = CafeStatusSuccessText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onVolver,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CafeBorderColor)
                    ) {
                        Text("Cancelar", color = CafeTextPrimary, fontSize = 14.sp)
                    }

                    Box(modifier = Modifier.weight(1.5f)) {
                        PrimarySolidButton(
                            text = if (state.isLoading) "Guardando..." else "Actualizar contraseña",
                            onClick = { viewModel.cambiarContrasena() },
                            enabled = !state.isLoading
                        )
                    }
                }
            }
        }
    }
}
