package com.flowbytestudio.core.domain

interface EventRepository {
    suspend fun getEvents(upcoming: Boolean? = null): Result<List<Event>>
    suspend fun getEvent(id: String): Result<Event>
    suspend fun getMyTickets(): Result<List<Ticket>>
    suspend fun getTicket(id: String): Result<Ticket>
}
