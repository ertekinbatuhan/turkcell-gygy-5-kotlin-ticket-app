package com.flowbytestudio.ticketapp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.flowbytestudio.core.domain.AuthRepository
import com.flowbytestudio.ticketapp.screen.LoginScreen
import com.flowbytestudio.ticketapp.screen.RegisterScreen
import org.koin.compose.koinInject
import com.flowbytestudio.core.domain.UserRole

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    authRepository: AuthRepository = koinInject()
) {
    val isLoggedIn by authRepository.isLoggedIn.collectAsStateWithLifecycle(initialValue = null)
    val userRole by authRepository.userRole.collectAsStateWithLifecycle(initialValue = null)

    when (isLoggedIn) {
        null -> SplashScreen()
        true -> AuthedNavHost(navController = navController, userRole = userRole, authRepository = authRepository)
        false -> UnAuthedNavHost(navController)
    }
}

@Composable
private fun SplashScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun AuthedNavHost(
    navController: NavHostController,
    userRole: UserRole?,
    authRepository: AuthRepository
) {
    val roleGraph = RoleNavGraph.fromRole(userRole)
    NavHost(navController = navController, startDestination = roleGraph.startDestination) {
        with(roleGraph) {
            registerDestinations(navController, authRepository)
        }
    }
}

@Composable
private fun UnAuthedNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Login) {
        composable<Login> {
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = { navController.navigate(Register) }
            )
        }
        composable<Register> {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate(Login) }
            )
        }
    }
}
