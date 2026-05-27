package com.flowbytestudio.core.domain.purchase

import com.flowbytestudio.core.domain.Ticket

enum class PurchaseStatus { PENDING, PAID, FAILED }

data class Purchase(
    val id: String,
    val status: PurchaseStatus,
    val totalCents: Int,
    val paidAt: String?,
    val items: List<PurchaseItem>,
    val tickets: List<Ticket>
)

data class PurchaseItem(
    val id: String,
    val ticketTypeId: String,
    val quantity: Int,
    val unitPriceCents: Int
)
