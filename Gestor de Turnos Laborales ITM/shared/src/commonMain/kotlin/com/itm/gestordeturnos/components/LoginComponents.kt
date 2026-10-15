package com.itm.gestordeturnos.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.itm.gestordeturnos.CafeBorderColor
import com.itm.gestordeturnos.CafeDarkBrown
import com.itm.gestordeturnos.CafeStatusErrorBg
import com.itm.gestordeturnos.CafeStatusErrorText
import com.itm.gestordeturnos.CafeStatusSuccessBg
import com.itm.gestordeturnos.CafeStatusSuccessText
import com.itm.gestordeturnos.CafeTextPrimary
import com.itm.gestordeturnos.CafeTextSecondary
import com.itm.gestordeturnos.viewmodel.FeedbackRecuperacion

@Composable
fun LabeledInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    modifier: Modifier = Modifier,
){
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = label.uppercase(),
            color = CafeTextSecondary,
            fontSize = 12.sp,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = CafeTextSecondary.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = CafeBorderColor,
                focusedBorderColor = CafeDarkBrown,
                unfocusedContainerColor = Color.Transparent,
                focusedLabelColor = Color.Transparent
            ),
            trailingIcon = if (isPassword) {
                {
                    TextButton(
                        onClick = { passwordVisible = !passwordVisible }
                    ){
                        Text(
                            text = if(passwordVisible) "OCULTAR" else "VER",
                            color = CafeTextSecondary,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }else null,
            shape = MaterialTheme.shapes.extraSmall
        )
    }
}

@Composable
fun PrimarySolidButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
){
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CafeDarkBrown),
        shape = MaterialTheme.shapes.extraSmall
    ){
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun GoogleLoginButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
){
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
    ){
        Text(
            "G Continuar con Google",
            color = if (enabled) CafeTextPrimary else CafeTextSecondary.copy(alpha = 0.5f)
        )
    }
}

/**
 * Modal emergente de feedback para la recuperación de contraseña.
 * Despliega información clara, icono semántico y mensaje según el resultado.
 */
@Composable
fun ModalFeedbackRecuperacion(
    feedback: FeedbackRecuperacion,
    onDismiss: () -> Unit
) {
    val isExito = feedback is FeedbackRecuperacion.Exito

    val iconBgColor = if (isExito) CafeStatusSuccessBg else CafeStatusErrorBg
    val iconColor = if (isExito) CafeStatusSuccessText else CafeStatusErrorText
    val iconVector = if (isExito) Icons.Default.Check else Icons.Default.Close

    val titulo = when (feedback) {
        is FeedbackRecuperacion.Exito -> feedback.titulo
        is FeedbackRecuperacion.ErrorNoEncontrado -> feedback.titulo
    }

    val mensaje = when (feedback) {
        is FeedbackRecuperacion.Exito -> feedback.mensaje
        is FeedbackRecuperacion.ErrorNoEncontrado -> feedback.mensaje
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, CafeBorderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono circular semántico
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(iconBgColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Título
                Text(
                    text = titulo,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeTextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Mensaje descriptivo
                Text(
                    text = mensaje,
                    fontSize = 14.sp,
                    color = CafeTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botón de confirmación / cerrar
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = CafeDarkBrown),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = "Entendido",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
