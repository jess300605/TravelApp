package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.AuthRepository
import com.example.data.DestinationRepository
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.DestinationFormScreen
import com.example.ui.screens.LoginScreen

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Catalog : Screen("catalog")
    data object CreateDestination : Screen("destination_create")
    data object EditDestination : Screen("destination_edit/{destinationId}") {
        fun createRoute(destinationId: String) = "destination_edit/$destinationId"
    }
}

@Composable
fun AppNavigation(
    authRepository: AuthRepository,
    destinationRepository: DestinationRepository,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val currentUser by authRepository.currentUser.collectAsState()

    val startDestination = if (currentUser != null) Screen.Catalog.route else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                authRepository = authRepository,
                onLoginSuccess = {
                    navController.navigate(Screen.Catalog.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Catalog.route) {
            CatalogScreen(
                destinationRepository = destinationRepository,
                authRepository = authRepository,
                onCreateClick = {
                    navController.navigate(Screen.CreateDestination.route)
                },
                onEditClick = { destinationId ->
                    navController.navigate(Screen.EditDestination.createRoute(destinationId))
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Catalog.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.CreateDestination.route) {
            DestinationFormScreen(
                destinationId = null,
                destinationRepository = destinationRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.EditDestination.route,
            arguments = listOf(
                navArgument("destinationId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val destinationId = backStackEntry.arguments?.getString("destinationId")
            DestinationFormScreen(
                destinationId = destinationId,
                destinationRepository = destinationRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
