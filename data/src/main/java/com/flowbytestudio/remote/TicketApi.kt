package com.flowbytestudio.remote

import com.flowbytestudio.data.dto.EventDto
import com.flowbytestudio.data.dto.TicketDto
import retrofit2.http.GET
import retrofit2.http.Query

interface TicketApi {
    @GET("/events")
    suspend fun getEvents(@Query("upcoming") upcoming: Boolean? = true): List<EventDto>

    @GET("/me/tickets")
    suspend fun getMyTickets(): List<TicketDto>
}
