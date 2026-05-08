package com.flowbytestudio.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CredentialsDto(val email : String, val password : String) {
}