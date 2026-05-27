package com.flowbytestudio.remote

import com.flowbytestudio.data.dto.EventDto
import com.flowbytestudio.data.dto.TicketDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface EventApi {
    @GET("/events")
    suspend fun getEvents(@Query("upcoming") upcoming: Boolean? = null): List<EventDto>

    @GET("/events/{id}")
    suspend fun getEvent(@Path("id") id: String): EventDto

    @GET("/me/tickets")
    suspend fun getMyTickets(): List<TicketDto>

    @GET("/me/tickets/{id}")
    suspend fun getTicket(@Path("id") id: String): TicketDto
}
