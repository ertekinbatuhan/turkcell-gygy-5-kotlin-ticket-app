package com.flowbytestudio.repository

import com.flowbytestudio.core.domain.Event
import com.flowbytestudio.core.domain.Ticket
import com.flowbytestudio.core.domain.TicketRepository
import com.flowbytestudio.data.mapper.toDomain
import com.flowbytestudio.data.util.runCatchingApi
import com.flowbytestudio.remote.TicketApi

class TicketRepositoryImpl(
    private val ticketApi: TicketApi,
) : TicketRepository {
    override suspend fun getEvents(upcoming: Boolean?): Result<List<Event>> =
        runCatchingApi { ticketApi.getEvents(upcoming = upcoming) }
            .map { events -> events.map { it.toDomain() } }

    override suspend fun getMyTickets(): Result<List<Ticket>> =
        runCatchingApi { ticketApi.getMyTickets() }
            .map { tickets -> tickets.map { it.toDomain() } }
}
