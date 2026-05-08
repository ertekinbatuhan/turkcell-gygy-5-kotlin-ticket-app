package com.flowbytestudio.data.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
data class RefreshRequestDto(val refreshToken : String) {
}