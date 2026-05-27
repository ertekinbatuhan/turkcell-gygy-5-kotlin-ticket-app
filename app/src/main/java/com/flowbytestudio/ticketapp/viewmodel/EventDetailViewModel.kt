package com.flowbytestudio.ticketapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowbytestudio.core.domain.Event
import com.flowbytestudio.core.domain.EventRepository
import com.flowbytestudio.core.domain.purchase.Purchase
import com.flowbytestudio.core.domain.purchase.PurchaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.flowbytestudio.ticketapp.util.toPurchaseUserMessage
import androidx.lifecycle.SavedStateHandle

data class EventDetailUiState(
    val isLoading: Boolean = true,
    val event: Event? = null,
    val quantities: Map<String, Int> = emptyMap(), // ticketTypeId -> quantity
    val isCreatingPurchase: Boolean = false,
    val createdPurchase: Purchase? = null,
    val isPayingPurchase: Boolean = false,
    val purchaseSuccess: Boolean = false,
    val errorMessage: String? = null,
    val showPaymentConfirmation: Boolean = false
) {
    val totalPriceCents: Int
        get() {
            val ev = event ?: return 0
            return ev.ticketTypes.sumOf { ticketType ->
                val qty = quantities[ticketType.id] ?: 0
                qty * ticketType.priceCents
            }
        }

    val canPurchase: Boolean
        get() = quantities.values.any { it > 0 } && !isLoading && !isCreatingPurchase
}

class EventDetailViewModel(
    private val eventRepository: EventRepository,
    private val purchaseRepository: PurchaseRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventId: String = savedStateHandle.get<String>("id") ?: ""

    private val _state = MutableStateFlow(EventDetailUiState())
    val state: StateFlow<EventDetailUiState> = _state.asStateFlow()

    init {
        loadEventDetails()
    }

    fun loadEventDetails() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            eventRepository.getEvent(eventId)
                .onSuccess { event ->
                    val initialQuantities = event.ticketTypes.associate { it.id to 0 }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            event = event,
                            quantities = initialQuantities,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Etkinlik detayları yüklenemedi."
                        )
                    }
                }
        }
    }

    fun onQuantityChange(ticketTypeId: String, quantity: Int) {
        val event = _state.value.event ?: return
        val ticketType = event.ticketTypes.find { it.id == ticketTypeId } ?: return
        val remaining = ticketType.remaining
        val maxSelectable = minOf(20, remaining)
        val validQty = quantity.coerceIn(0, maxSelectable)

        _state.update { current ->
            val updatedMap = current.quantities.toMutableMap()
            updatedMap[ticketTypeId] = validQty
            current.copy(quantities = updatedMap)
        }
    }

    fun createPurchase() {
        val current = _state.value
        if (!current.canPurchase) return

        val activeSelections = current.quantities.filter { it.value > 0 }
        if (activeSelections.isEmpty()) return

        _state.update { it.copy(isCreatingPurchase = true, errorMessage = null) }

        viewModelScope.launch {
            purchaseRepository.createPurchase(activeSelections)
                .onSuccess { purchase ->
                    _state.update {
                        it.copy(
                            isCreatingPurchase = false,
                            createdPurchase = purchase,
                            showPaymentConfirmation = true,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isCreatingPurchase = false,
                            errorMessage = error.toPurchaseUserMessage()
                        )
                    }
                }
        }
    }

    fun confirmPayment() {
        val purchaseId = _state.value.createdPurchase?.id ?: return
        _state.update { it.copy(isPayingPurchase = true, errorMessage = null) }

        viewModelScope.launch {
            purchaseRepository.payPurchase(purchaseId)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isPayingPurchase = false,
                            showPaymentConfirmation = false,
                            purchaseSuccess = true,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isPayingPurchase = false,
                            errorMessage = error.toPurchaseUserMessage()
                        )
                    }
                }
        }
    }

    fun cancelPayment() {
        _state.update {
            it.copy(
                showPaymentConfirmation = false,
                createdPurchase = null
            )
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
