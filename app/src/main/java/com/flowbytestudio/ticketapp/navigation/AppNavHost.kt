package com.flowbytestudio.ticketapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.flowbytestudio.ticketapp.screen.LoginScreen
import com.flowbytestudio.ticketapp.screen.RegisterScreen


@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    //authRepository: AuthRepository = koinInject()
)
{
    NavHost(navController = navController, startDestination = Login) {
        composable<Login> {
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = { navController.navigate(Register) }
            )
        }
        composable<Register> {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
    }
}
