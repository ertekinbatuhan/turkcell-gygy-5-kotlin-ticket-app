package com.flowbytestudio.ticketapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowbytestudio.core.domain.Event
import com.flowbytestudio.core.domain.Ticket
import com.flowbytestudio.core.domain.TicketRepository
import com.flowbytestudio.ticketapp.util.HomeErrorContext
import com.flowbytestudio.ticketapp.util.toHomeUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HomeTab(val title: String) {
    Events("Etkinlikler"),
    Tickets("Biletlerim"),
}

data class HomeUiState(
    val selectedTab: HomeTab = HomeTab.Events,
    val events: List<Event> = emptyList(),
    val tickets: List<Ticket> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)

class HomeViewModel(
    private val ticketRepository: TicketRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun selectTab(tab: HomeTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    fun refresh() {
        if (_state.value.isRefreshing) return

        _state.update {
            it.copy(
                isLoading = it.events.isEmpty() && it.tickets.isEmpty(),
                isRefreshing = true,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            val eventsResult = ticketRepository.getEvents(upcoming = true)
            val ticketsResult = ticketRepository.getMyTickets()

            val errorMessage = listOfNotNull(
                eventsResult.exceptionOrNull()?.toHomeUserMessage(HomeErrorContext.Events),
                ticketsResult.exceptionOrNull()?.toHomeUserMessage(HomeErrorContext.Tickets),
            ).joinToString(separator = "\n").ifBlank { null }

            _state.update { current ->
                current.copy(
                    events = eventsResult.getOrElse { current.events },
                    tickets = ticketsResult.getOrElse { current.tickets },
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = errorMessage,
                )
            }
        }
    }
}
