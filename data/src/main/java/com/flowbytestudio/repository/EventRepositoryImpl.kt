package com.flowbytestudio.repository

import com.flowbytestudio.core.domain.Event
import com.flowbytestudio.core.domain.Ticket
import com.flowbytestudio.core.domain.EventRepository
import com.flowbytestudio.data.mapper.toDomain
import com.flowbytestudio.data.util.runCatchingApi
import com.flowbytestudio.remote.EventApi

class EventRepositoryImpl(
    private val eventApi: EventApi,
) : EventRepository {
    override suspend fun getEvents(upcoming: Boolean?): Result<List<Event>> =
        runCatchingApi { eventApi.getEvents(upcoming = upcoming) }
            .map { events -> events.map { it.toDomain() } }

    override suspend fun getEvent(id: String): Result<Event> =
        runCatchingApi { eventApi.getEvent(id = id) }
            .map { it.toDomain() }

    override suspend fun getMyTickets(): Result<List<Ticket>> =
        runCatchingApi { eventApi.getMyTickets() }
            .map { tickets -> tickets.map { it.toDomain() } }

    override suspend fun getTicket(id: String): Result<Ticket> =
        runCatchingApi { eventApi.getTicket(id = id) }
            .map { it.toDomain() }
}
