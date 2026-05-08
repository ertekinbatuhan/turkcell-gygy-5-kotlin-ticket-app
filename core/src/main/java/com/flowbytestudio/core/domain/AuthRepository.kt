package com.flowbytestudio.core.domain

import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    val isLoggedIn : Flow<Boolean>

    suspend fun login(email : String, password : String) : Result<AuthSession>
    suspend fun register(email : String, password : String) : Result<AuthSession>
    suspend fun logout(): Result<Unit>
 }