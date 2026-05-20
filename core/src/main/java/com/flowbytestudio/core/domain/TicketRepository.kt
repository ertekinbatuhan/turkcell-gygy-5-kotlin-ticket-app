package com.flowbytestudio.core.domain

interface TicketRepository {
    suspend fun getEvents(upcoming: Boolean? = true): Result<List<Event>>
    suspend fun getMyTickets(): Result<List<Ticket>>
}
