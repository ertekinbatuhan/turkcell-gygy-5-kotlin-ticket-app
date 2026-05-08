package com.flowbytestudio.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(val id : String, val email : String, val role : String) {

}