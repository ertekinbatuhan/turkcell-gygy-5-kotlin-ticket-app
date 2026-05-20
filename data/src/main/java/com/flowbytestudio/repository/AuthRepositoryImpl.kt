package com.flowbytestudio.repository

import com.flowbytestudio.core.domain.AuthRepository
import com.flowbytestudio.core.domain.AuthSession
import com.flowbytestudio.core.domain.User
import com.flowbytestudio.core.domain.UserRole
import com.flowbytestudio.data.dto.CredentialsDto
import com.flowbytestudio.data.dto.TokenPairDto
import com.flowbytestudio.data.local.TokenStore
import com.flowbytestudio.data.util.runCatchingApi
import com.flowbytestudio.remote.AuthApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val tokenStore: TokenStore
) : AuthRepository {
    override val isLoggedIn: Flow<Boolean> = tokenStore.accessToken.map { it != null }

    override suspend fun login(
        email: String,
        password: String
    ): Result<AuthSession> = runCatchingApi {
        authApi.login(CredentialsDto(email=email, password=password))
    }.onSuccess {
        tokenStore.save(it.accessToken, it.refreshToken)
    }
        .map {
                tokenPairDto -> AuthSession(
            user = User(
                tokenPairDto.user.id, tokenPairDto.user.email, UserRole.fromApi(tokenPairDto.user.role),
            ),
            accessToken = tokenPairDto.accessToken,
            refreshToken = tokenPairDto.refreshToken)
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