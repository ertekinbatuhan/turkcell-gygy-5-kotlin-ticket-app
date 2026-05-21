package com.flowbytestudio.core.domain

interface EventRepository {
    suspend fun getEvents(upcoming: Boolean? = null): Result<List<Event>>
    suspend fun getMyTickets(): Result<List<Ticket>>
}
