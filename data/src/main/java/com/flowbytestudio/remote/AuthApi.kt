package com.flowbytestudio.remote

import com.flowbytestudio.data.dto.CredentialsDto
import com.flowbytestudio.data.dto.RefreshRequestDto
import com.flowbytestudio.data.dto.TokenPairDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("/auth/login")
    suspend fun login(@Body body : CredentialsDto) : TokenPairDto

    @POST("/auth/register")
    suspend fun register(@Body body : CredentialsDto) : TokenPairDto

    @POST("/auth/refresh")
    suspend fun refresh(@Body body : RefreshRequestDto) : TokenPairDto
}