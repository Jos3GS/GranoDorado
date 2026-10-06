package com.itm.gestordeturnos.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.itm.gestordeturnos.CafeAccentYellow
import com.itm.gestordeturnos.CafeBackgroundLight
import com.itm.gestordeturnos.CafeDarkBrown
import com.itm.gestordeturnos.components.DrawerMenuItem
import com.itm.gestordeturnos.components.UserProfileInfo
import com.itm.gestordeturnos.viewmodel.AsistenciaViewModel
import com.itm.gestordeturnos.viewmodel.CambiosTurnoViewModel
import com.itm.gestordeturnos.viewmodel.EmpleadosViewModel
import com.itm.gestordeturnos.viewmodel.MainViewModel
import com.itm.gestordeturnos.viewmodel.MallaSemanalViewModel
import com.itm.gestordeturnos.viewmodel.MiHorarioViewModel
import com.itm.gestordeturnos.viewmodel.NovedadesViewModel
import com.itm.gestordeturnos.viewmodel.TurnosViewModel
import com.itm.gestordeturnos.viewmodel.UsuarioActual
import kotlinx.coroutines.launch

@Composable
fun MainScreen(viewModel: MainViewModel, onLogoutSuccess: () -> Unit) {
    val state by viewModel.uiState.collectAsState()

    val internalNavController = rememberNavController()

    val navBackStackEntry by internalNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "panel"

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val menuLateral = @Composable {
        Column(
            modifier = Modifier
                .width(280.dp)
                .fillMaxHeight()
                .background(CafeDarkBrown)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.padding(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).background(CafeAccentYellow),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("()", fontWeight = FontWeight.Bold, color = CafeDarkBrown)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Grano Dorado", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("GESTIÓN DE TURNOS", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, letterSpacing = 1.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))

                state.opcionesMenu.forEach { opcion ->
                    DrawerMenuItem(
                        titulo = opcion.titulo,
                        isSelected = opcion.id == currentRoute,
                        onClick = {
                            internalNavController.navigate(opcion.id) {
                                popUpTo(internalNavController.graph.findStartDestination().route ?: "panel") {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }

            state.usuario?.let { usuario ->
                UserProfileInfo(
                    nombre = usuario.nombre,
                    iniciales = usuario.iniciales,
                    cargo = usuario.cargo,
                    onLogoutClick = { onLogoutSuccess() }
                )
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(CafeBackgroundLight)
    ) {
        val isCompact = maxWidth < 800.dp

        if (isCompact) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(drawerContainerColor = Color.Transparent) { menuLateral() }
                }
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopAppBar(
                        title = { Text("Grano Dorado", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (currentRoute == "empleados") Modifier else Modifier.padding(16.dp))
                    ) {
                        DashboardNavHost(internalNavController, state.usuario)
                    }
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                menuLateral()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .then(if (currentRoute == "empleados") Modifier else Modifier.padding(32.dp))
                ) {
                    DashboardNavHost(internalNavController, state.usuario)
                }
            }
        }
    }
}

@Composable
fun DashboardNavHost(navController: NavHostController, usuario: UsuarioActual?) {
    NavHost(
        navController = navController,
        startDestination = "panel"
    ) {
        composable("panel") {
            usuario?.let { PanelScreen(usuario = it) }
        }
        composable("empleados") {
            EmpleadosScreen(viewModel = viewModel { EmpleadosViewModel() })
        }
        composable("malla") {
            MallaSemanalScreen(viewModel = viewModel { MallaSemanalViewModel() })
        }
        composable("mi_horario") {
            MiHorarioScreen(viewModel = viewModel { MiHorarioViewModel() })
        }

        // Pantallas en construcción
        composable("turnos") { TurnosScreen(viewModel = viewModel { TurnosViewModel() }) }
        composable("novedades") { NovedadesScreen(viewModel = viewModel { NovedadesViewModel() }) }
        composable("cambios") { CambiosTurnoScreen(viewModel = viewModel { CambiosTurnoViewModel() }) }
        composable("cambios_empleado") { CambiosTurnoScreen(viewModel = viewModel { CambiosTurnoViewModel() }) }
        composable("asistencia") { AsistenciaScreen(viewModel = viewModel { AsistenciaViewModel() }) }
        composable("reportar_novedad") { Text("Pantalla de Reportar Novedad", fontSize = 24.sp) }
        composable("marcar_asistencia") { Text("Pantalla de Marcar Asistencia", fontSize = 24.sp) }
    }
}
