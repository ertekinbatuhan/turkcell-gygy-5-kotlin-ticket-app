package com.flowbytestudio.ticketapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowbytestudio.core.domain.AuthRepository
import com.flowbytestudio.core.domain.Event
import com.flowbytestudio.core.domain.EventRepository
import com.flowbytestudio.core.domain.Ticket
import com.flowbytestudio.core.domain.TicketType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface StaffUiEffect {
    object RequestCameraPermission : StaffUiEffect
    object OpenGallery : StaffUiEffect
    data class ShowToast(val message: String) : StaffUiEffect
}

data class StaffUiState(
    val isProcessing: Boolean = false,
    val scannedResult: String? = null,
    val showMethodDialog: Boolean = false,
    val showResultDialog: Boolean = false,
    val isValidating: Boolean = false,
    val validationResult: TicketValidationResult? = null
)

class StaffViewModel(
    private val eventRepository: EventRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StaffUiState())
    val state: StateFlow<StaffUiState> = _state.asStateFlow()

    private val _effect = Channel<StaffUiEffect>(Channel.BUFFERED)
    val effect: Flow<StaffUiEffect> = _effect.receiveAsFlow()

    fun showMethodDialog(show: Boolean) {
        _state.update { it.copy(showMethodDialog = show) }
    }

    fun showResultDialog(show: Boolean) {
        _state.update { it.copy(showResultDialog = show) }
    }

    fun setScannedResult(result: String?) {
        _state.update {
            it.copy(
                scannedResult = result,
                showResultDialog = result != null,
                validationResult = null
            )
        }
    }

    fun setProcessing(processing: Boolean) {
        _state.update { it.copy(isProcessing = processing) }
    }

    fun triggerCameraScan() {
        viewModelScope.launch {
            _effect.send(StaffUiEffect.RequestCameraPermission)
        }
    }

    fun triggerGalleryScan() {
        viewModelScope.launch {
            _effect.send(StaffUiEffect.OpenGallery)
        }
    }

    fun showToast(message: String) {
        viewModelScope.launch {
            _effect.send(StaffUiEffect.ShowToast(message))
        }
    }

    fun validateTicket(ticketId: String) {
        _state.update { it.copy(isValidating = true) }
        viewModelScope.launch {
            val ticketResult = eventRepository.getTicket(ticketId)
            if (ticketResult.isSuccess) {
                val ticket = ticketResult.getOrThrow()
                // Fetch events to match ticketType and event name
                val eventsResult = eventRepository.getEvents(upcoming = null)
                val events = eventsResult.getOrNull()

                val matchingEvent = events?.find { event ->
                    event.ticketTypes.any { it.id == ticket.ticketTypeId }
                }
                val matchingTicketType = matchingEvent?.ticketTypes?.find { it.id == ticket.ticketTypeId }

                _state.update {
                    it.copy(
                        isValidating = false,
                        validationResult = TicketValidationResult.Success(
                            ticket = ticket,
                            event = matchingEvent,
                            ticketType = matchingTicketType
                        )
                    )
                }
            } else {
                val errorMsg = ticketResult.exceptionOrNull()?.message ?: "Bilet bulunamadı."
                _state.update {
                    it.copy(
                        isValidating = false,
                        validationResult = TicketValidationResult.Invalid(errorMsg)
                    )
                }
            }
        }
    }

    fun clearValidationResult() {
        _state.update { it.copy(validationResult = null) }
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLogoutSuccess()
        }
    }
}

sealed interface TicketValidationResult {
    data class Success(
        val ticket: Ticket,
        val event: Event?,
        val ticketType: TicketType?
    ) : TicketValidationResult

    data class Invalid(val message: String) : TicketValidationResult
}
