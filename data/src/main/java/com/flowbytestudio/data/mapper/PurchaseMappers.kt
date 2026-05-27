package com.flowbytestudio.data.mapper

import com.flowbytestudio.core.domain.purchase.Purchase
import com.flowbytestudio.core.domain.purchase.PurchaseItem
import com.flowbytestudio.core.domain.purchase.PurchaseStatus
import com.flowbytestudio.data.dto.PurchaseDto
import com.flowbytestudio.data.dto.PurchaseItemDto

fun PurchaseItemDto.toDomain(): PurchaseItem = PurchaseItem(
    id = id,
    ticketTypeId = ticketTypeId,
    quantity = quantity,
    unitPriceCents = unitPriceCents
)

fun PurchaseDto.toDomain(): Purchase = Purchase(
    id = id,
    status = when (status.uppercase()) {
        "PAID" -> PurchaseStatus.PAID
        "FAILED" -> PurchaseStatus.FAILED
        else -> PurchaseStatus.PENDING
    },
    totalCents = totalCents,
    paidAt = paidAt,
    items = items.map { it.toDomain() },
    tickets = tickets.map { it.toDomain() }
)
