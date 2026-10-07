package com.itm.gestordeturnos.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itm.gestordeturnos.CafeAccentYellow
import com.itm.gestordeturnos.CafeBackgroundLight
import com.itm.gestordeturnos.CafeDarkBrown
import com.itm.gestordeturnos.CafeTextPrimary
import com.itm.gestordeturnos.CafeTextSecondary
import com.itm.gestordeturnos.components.GoogleLoginButton
import com.itm.gestordeturnos.components.LabeledInputField
import com.itm.gestordeturnos.components.PrimarySolidButton
import com.itm.gestordeturnos.viewmodel.LoginState
import com.itm.gestordeturnos.viewmodel.LoginViewModel

@Composable
fun LoginScreen(viewModel: LoginViewModel, onLoginSuccess: () -> Unit) {
    val state by viewModel.uiState.collectAsState()



    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(CafeBackgroundLight)
    ) {
        val isWideScreen = maxWidth > 800.dp

        if(isWideScreen) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(CafeDarkBrown)
                        .padding(48.dp)
                ){
                    PanelBrandingOscuro()
                }
                Box(
                    modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ){
                    FormularioLogin(state, viewModel, mostrarLogoArriba = false,onLoginSuccess)
                }
            }
        }else{
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ){
                FormularioLogin(state, viewModel, mostrarLogoArriba = true,onLoginSuccess)
            }
        }
    }
}

@Composable
fun FormularioLogin(state: LoginState, viewModel: LoginViewModel, mostrarLogoArriba: Boolean, onLoginSuccess: () -> Unit) {
    Column(
        modifier = Modifier.widthIn(max = 400.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ){
        if(mostrarLogoArriba){
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Box(
                    modifier = Modifier.size(32.dp).background(CafeAccentYellow),
                    contentAlignment = Alignment.Center
                ){
                    Text("()", fontWeight = FontWeight.Bold, color = CafeDarkBrown)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Grano Dorado", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = CafeTextPrimary)
            }
        }

        Column(
            modifier = Modifier.padding(bottom = 8.dp)
        ){
            Text(
                "INICIAR SESIÓN",
                color = CafeTextSecondary,
                fontSize = 12.sp,
                letterSpacing = 1.sp)
            Text(
                "Bienvenido de vuelta",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CafeTextPrimary)
        }

        LabeledInputField(
            label = "Correo Electrónico",
            value = state.email,
            onValueChange = {viewModel.onEmailChanged(it)},
            placeholder = "tu.correo@granodorado.com"
        )

        LabeledInputField(
            label = "Contraseña",
            value = state.password,
            onValueChange = {viewModel.onPasswordChanged(it)},
            placeholder = "********",
            isPassword = true
        )

        PrimarySolidButton(
            text = "Iniciar sesión",
            onClick = {
                viewModel.iniciarSesion()
                onLoginSuccess()
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = {}
            ){
                Text(
                    "¿Olvidaste tu clave?",
                    color = CafeTextSecondary)
            }
            GoogleLoginButton(onClick = {
                viewModel.loginConGoogle()
                onLoginSuccess()
            })
        }
    }
}

@Composable
fun PanelBrandingOscuro() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ){
            Box(
                modifier = Modifier.size(40.dp)
                    .background(CafeAccentYellow),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = "()",
                    fontWeight = FontWeight.Bold,
                    color = CafeDarkBrown
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column{
                Text(
                    text = "Grano Dorado",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "GESTIÓN DE TURNOS",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
            }
        }

        Column{
            Text(
                text = "El horario deja de perderse porque deja de ser un mensaje.",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 40.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "Un acceso individual para las 3 sucursales. Planifica turnos, verifica cobertura y deja atrás el grupo de WhatsApp y las hojas de Excel.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(48.dp)
        ){
            StatItem("45", "EMPLEADOS")
            StatItem("3","SUCURSALES")
            StatItem("06-22", "FRANJA")
        }
    }
}

@Composable
fun StatItem(number: String, label: String) {
    Column{
        Text(
            text = number,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            letterSpacing = 1.sp
        )
    }
}