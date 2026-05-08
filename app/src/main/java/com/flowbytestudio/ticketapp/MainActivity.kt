package com.flowbytestudio.ticketapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.flowbytestudio.core.domain.AuthRepository
import com.flowbytestudio.core.ui.theme.TicketAppTheme
import com.flowbytestudio.ticketapp.auth.LoginScreen
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val authRepository: AuthRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TicketAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LoginScreen(authRepository = authRepository)
                }
            }
        }
    }
}
