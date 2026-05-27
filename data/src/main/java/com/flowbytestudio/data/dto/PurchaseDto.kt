package com.flowbytestudio.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class PurchaseItemRequestDto(
    val ticketTypeId: String,
    val quantity: Int
)

@Serializable
data class PurchaseCreateDto(
    val items: List<PurchaseItemRequestDto>
)

@Serializable
data class PurchaseItemDto(
    val id: String = "",
    val ticketTypeId: String = "",
    val quantity: Int = 0,
    val unitPriceCents: Int = 0
)

@Serializable
data class PurchaseDto(
    val id: String = "",
    val status: String = "",
    val totalCents: Int = 0,
    val paidAt: String? = null,
    val items: List<PurchaseItemDto> = emptyList(),
    val tickets: List<TicketDto> = emptyList()
)
