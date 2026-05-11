package com.flowbytestudio.ticketapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowbytestudio.core.domain.AuthRepository
import com.flowbytestudio.ticketapp.util.AuthErrorContext
import com.flowbytestudio.ticketapp.util.toAuthUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
) {
    val canSubmit: Boolean get() = email.isNotBlank() && password.length >= 8 && !isLoading
}

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) = _state.update {
        it.copy(email = value, errorMessage = null, successMessage = null)
    }

    fun onPasswordChange(value: String) = _state.update {
        it.copy(password = value, errorMessage = null, successMessage = null)
    }

    fun submit() {
        val current = _state.value
        if (!current.canSubmit) return

        _state.update {
            it.copy(isLoading = true, errorMessage = null, successMessage = null)
        }

        viewModelScope.launch {
            authRepository.register(current.email.trim(), current.password)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Başarılı bir şekilde kayıt oldunuz."
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.toAuthUserMessage(AuthErrorContext.Register)
                        )
                    }
                }
        }
    }
}
