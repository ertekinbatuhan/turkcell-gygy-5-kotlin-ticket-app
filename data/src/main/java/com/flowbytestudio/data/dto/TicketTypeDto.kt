package com.flowbytestudio.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class TicketTypeDto(
    val id: String = "",
    val name: String = "",
    val priceCents: Int = 0,
    val capacity: Int = 0,
    val soldCount: Int = 0,
    val remaining: Int = 0,
)
