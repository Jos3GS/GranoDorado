package com.itm.gestordeturnos

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.itm.gestordeturnos.screens.LoginScreen
import com.itm.gestordeturnos.screens.MainScreen
import com.itm.gestordeturnos.viewmodel.LoginViewModel
import com.itm.gestordeturnos.viewmodel.MainViewModel
import kotlinx.serialization.Serializable

/**
 * Rutas tipadas para la navegación raíz de la aplicación.
 */
@Serializable
object LoginRoute

@Serializable
object MainRoute

@Composable
@Preview
fun App() {
    remember { initPlatformFirebase() }

    val navController = rememberNavController()

    CafeAppTheme{
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ){
            NavHost(
                navController = navController,
                startDestination = LoginRoute
            ){
                composable<LoginRoute>{
                    val loginViewModel = viewModel { LoginViewModel() }
                    LoginScreen(
                        viewModel = loginViewModel,
                        onLoginSuccess = {
                            navController.navigate(MainRoute){
                                popUpTo<LoginRoute>{inclusive = true}
                            }
                        }
                    )
                }
                composable<MainRoute>{
                    val mainViewModel = viewModel { MainViewModel() }
                    MainScreen(
                        viewModel = mainViewModel,
                        onLogoutSuccess = {
                            navController.navigate(LoginRoute){
                                popUpTo<MainRoute>{inclusive = true}
                            }
                        }
                    )
                }
            }
        }
    }
}
