package com.flowbytestudio.repository

import com.flowbytestudio.core.domain.AuthRepository
import com.flowbytestudio.core.domain.AuthSession
import com.flowbytestudio.core.domain.User
import com.flowbytestudio.core.domain.UserRole
import com.flowbytestudio.data.dto.CredentialsDto
import com.flowbytestudio.data.util.runCatchingApi
import com.flowbytestudio.remote.AuthApi
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl(
    private val authApi: AuthApi
) : AuthRepository {
    override val isLoggedIn: Flow<Boolean>
        get() = TODO("Not yet implemented")

    override suspend fun login(
        email: String,
        password: String
    ): Result<AuthSession> = runCatchingApi {
        authApi.login(CredentialsDto(email = email, password = password))
    }.onSuccess {

    }
        .map {
            i -> AuthSession(
            user = User(
                i.user.id, i.user.email, UserRole.fromApi(i.user.role),
            ),
            accessToken = i.accessToken,
            refreshToken = i.refreshToken)
        }

    override suspend fun register(
        email: String,
        password: String
    ): Result<AuthSession> {
        TODO("Not yet implemented")
    }

    override suspend fun logout(): Result<Unit> {
        TODO("Not yet implemented")
    }
}