package com.flowbytestudio.core.domain

data class Ticket(
    val id: String,
    val qrCode: String,
    val status: TicketStatus,
    val ticketTypeId: String,
)

enum class TicketStatus {
    VALID,
    USED;

    companion object {
        fun fromApi(value: String?): TicketStatus = when (value?.uppercase()) {
            "USED" -> USED
            else -> VALID
        }
    }
}
