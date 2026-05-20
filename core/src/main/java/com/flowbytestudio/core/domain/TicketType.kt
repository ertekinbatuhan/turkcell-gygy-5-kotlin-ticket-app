package com.flowbytestudio.core.domain

data class TicketType(
    val id: String,
    val name: String,
    val priceCents: Int,
    val capacity: Int,
    val soldCount: Int,
    val remaining: Int,
)
