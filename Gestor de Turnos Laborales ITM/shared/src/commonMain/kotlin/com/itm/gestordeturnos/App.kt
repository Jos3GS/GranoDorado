package com.itm.gestordeturnos

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.itm.gestordeturnos.screens.LoginScreen
import com.itm.gestordeturnos.screens.MainScreen
import com.itm.gestordeturnos.viewmodel.LoginViewModel
import com.itm.gestordeturnos.viewmodel.MainViewModel
import com.itm.gestordeturnos.viewmodel.MiHorarioViewModel

@Composable
@Preview
fun App() {
    val loginViewModel = remember { LoginViewModel() }
    val mainViewModel = remember { MainViewModel() }
    val miHorarioViewModel = remember { MiHorarioViewModel() }

    val navController = rememberNavController()

    CafeAppTheme{
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ){
            NavHost(
                navController = navController,
                startDestination = "login"
            ){
                composable("login"){
                    LoginScreen(
                        viewModel = loginViewModel,
                        onLoginSuccess = {
                            navController.navigate("main"){
                                popUpTo("login"){inclusive = true}
                            }
                        }
                    )
                }
                composable("main"){
                    MainScreen(
                        viewModel = mainViewModel,
                        onLogoutSuccess = {
                            navController.navigate("login"){
                                popUpTo("main"){inclusive = true}
                            }
                        }
                    )
                }
            }
        }
    }
}