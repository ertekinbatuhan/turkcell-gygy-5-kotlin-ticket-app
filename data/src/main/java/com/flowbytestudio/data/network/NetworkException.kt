package com.flowbytestudio.data.network

// Bağlantı kopuk, timeout, dns çözümleme
class NetworkException(cause : Throwable): RuntimeException("NetworkError",cause){
}

class ApiException(
    val code : Int,
    val errorMessage : String?,
    cause : Throwable? = null

) : RuntimeException("HTTP $code: $errorMessage",cause){

}