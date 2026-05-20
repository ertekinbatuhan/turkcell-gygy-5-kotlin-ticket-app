package com.flowbytestudio.data.mapper

import com.flowbytestudio.core.domain.Event
import com.flowbytestudio.core.domain.Ticket
import com.flowbytestudio.core.domain.TicketStatus
import com.flowbytestudio.core.domain.TicketType
import com.flowbytestudio.data.dto.EventDto
import com.flowbytestudio.data.dto.TicketDto
import com.flowbytestudio.data.dto.TicketTypeDto

fun EventDto.toDomain(): Event = Event(
    id = id,
    name = name,
    description = description,
    venue = venue,
    startsAt = startsAt,
    endsAt = endsAt,
    ticketTypes = ticketTypes.map { it.toDomain() },
)

fun TicketTypeDto.toDomain(): TicketType = TicketType(
    id = id,
    name = name,
    priceCents = priceCents,
    capacity = capacity,
    soldCount = soldCount,
    remaining = remaining,
)

fun TicketDto.toDomain(): Ticket = Ticket(
    id = id,
    qrCode = qrCode,
    status = TicketStatus.fromApi(status),
    ticketTypeId = ticketTypeId,
)
