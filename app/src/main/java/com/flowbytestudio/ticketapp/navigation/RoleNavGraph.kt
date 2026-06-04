package com.flowbytestudio.ticketapp.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.flowbytestudio.core.domain.AuthRepository
import com.flowbytestudio.core.domain.UserRole
import com.flowbytestudio.ticketapp.screen.EventDetailScreen
import com.flowbytestudio.ticketapp.screen.HomeScreen
import com.flowbytestudio.ticketapp.screen.StaffScreen
import com.flowbytestudio.ticketapp.screen.TicketDetailScreen
import kotlinx.coroutines.launch

sealed interface RoleNavGraph {
    val startDestination: Any

    fun NavGraphBuilder.registerDestinations(
        navController: NavHostController,
        authRepository: AuthRepository
    )

    companion object {
        fun fromRole(role: UserRole?): RoleNavGraph = when (role) {
            UserRole.ADMIN -> AdminNavGraph
            UserRole.STAFF -> StaffNavGraph
            else -> UserNavGraph
        }
    }
}

object UserNavGraph : RoleNavGraph {
    override val startDestination: Any = Home

    override fun NavGraphBuilder.registerDestinations(
        navController: NavHostController,
        authRepository: AuthRepository
    ) {
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

object StaffNavGraph : RoleNavGraph {
    override val startDestination: Any = StaffHome

    override fun NavGraphBuilder.registerDestinations(
        navController: NavHostController,
        authRepository: AuthRepository
    ) {
        composable<StaffHome> {
            val scope = rememberCoroutineScope()
            StaffScreen(
                onLogout = {
                    scope.launch {
                        authRepository.logout()
                    }
                }
            )
        }
    }
}

object AdminNavGraph : RoleNavGraph {
    override val startDestination: Any = AdminHome

    override fun NavGraphBuilder.registerDestinations(
        navController: NavHostController,
        authRepository: AuthRepository
    ) {
        composable<AdminHome> {
            val scope = rememberCoroutineScope()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Admin Paneli (Yapım Aşamasında)", modifier = Modifier.padding(bottom = 16.dp))
                Button(onClick = {
                    scope.launch {
                        authRepository.logout()
                    }
                }) {
                    Text("Çıkış Yap")
                }
            }
        }
    }
}
