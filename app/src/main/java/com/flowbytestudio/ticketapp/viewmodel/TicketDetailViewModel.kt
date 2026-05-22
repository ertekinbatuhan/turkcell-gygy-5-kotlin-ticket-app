package com.flowbytestudio.ticketapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowbytestudio.core.domain.Event
import com.flowbytestudio.core.domain.Ticket
import com.flowbytestudio.core.domain.TicketType
import com.flowbytestudio.core.domain.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TicketDetailUiState(
    val isLoading: Boolean = true,
    val ticket: Ticket? = null,
    val event: Event? = null,
    val ticketType: TicketType? = null,
    val errorMessage: String? = null,
)

class TicketDetailViewModel(
    private val eventRepository: EventRepository,
    private val ticketId: String,
) : ViewModel() {
    private val _state = MutableStateFlow(TicketDetailUiState())
    val state: StateFlow<TicketDetailUiState> = _state.asStateFlow()

    init {
        loadTicketDetails()
    }

    fun loadTicketDetails() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val ticketsResult = eventRepository.getMyTickets()
            val eventsResult = eventRepository.getEvents(upcoming = null)

            val tickets = ticketsResult.getOrNull()
            val events = eventsResult.getOrNull()

            if (ticketsResult.isFailure || eventsResult.isFailure) {
                val error = listOfNotNull(
                    ticketsResult.exceptionOrNull()?.message,
                    eventsResult.exceptionOrNull()?.message
                ).joinToString("\n").ifBlank { "Bilet detayları yüklenemedi." }

                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error
                    )
                }
                return@launch
            }

            val ticket = tickets?.find { it.id == ticketId }
            if (ticket == null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Bilet bulunamadı."
                    )
                }
                return@launch
            }

            val matchingEvent = events?.find { event ->
                event.ticketTypes.any { it.id == ticket.ticketTypeId }
            }
            val matchingTicketType = matchingEvent?.ticketTypes?.find { it.id == ticket.ticketTypeId }

            _state.update {
                it.copy(
                    isLoading = false,
                    ticket = ticket,
                    event = matchingEvent,
                    ticketType = matchingTicketType,
                    errorMessage = null
                )
            }
        }
    }
}
