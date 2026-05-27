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
import androidx.navigation.internal.NavContext
import androidx.navigation.toRoute
import com.flowbytestudio.core.domain.AuthRepository
import com.flowbytestudio.ticketapp.navigation.TicketDetail
import com.flowbytestudio.ticketapp.screen.HomeScreen
import com.flowbytestudio.ticketapp.screen.LoginScreen
import com.flowbytestudio.ticketapp.screen.RegisterScreen
import com.flowbytestudio.ticketapp.screen.TicketDetailScreen
import com.flowbytestudio.ticketapp.screen.EventDetailScreen
import org.koin.compose.koinInject



@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    authRepository: AuthRepository = koinInject()
)
{
    val isLoggedIn by authRepository.isLoggedIn.collectAsStateWithLifecycle(initialValue = null)

    when(isLoggedIn)
    {
        null -> SplashScreen()
        true -> AuthedNavHost(navController)
        false -> UnAuthedNavHost(navController)
    }
}

@Composable
private fun SplashScreen(){
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        CircularProgressIndicator()
    }
}

@Composable
private fun AuthedNavHost(navController: NavHostController){
    NavHost(navController=navController, startDestination = Home){
        composable<Home> {
            HomeScreen(
                onTicketClick = { ticketId ->
                    navController.navigate(TicketDetail(ticketId = ticketId))
                },
                onEventClick = { eventId ->
                    navController.navigate(EventDetail(id = eventId))
                }
            )
        }
        composable<EventDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<EventDetail>()
            EventDetailScreen(
                eventId = route.id,
                onBackClick = { navController.navigateUp() },
                onPurchaseSuccess = {
                    navController.navigate(Home) {
                        popUpTo(Home) { inclusive = true }
                    }
                }
            )
        }
        composable<TicketDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<TicketDetail>()
            TicketDetailScreen(
                ticketId = route.ticketId,
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}

@Composable
private fun UnAuthedNavHost(navController: NavHostController){
    NavHost(navController=navController, startDestination = Login) {
        composable<Login>{
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = {navController.navigate(Register)}
            )
        }
        composable<Register> {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate(Login) }
            )
        }
    }
}
