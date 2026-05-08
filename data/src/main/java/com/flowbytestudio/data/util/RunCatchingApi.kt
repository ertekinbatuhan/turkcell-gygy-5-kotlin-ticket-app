package com.flowbytestudio.data.util


import com.flowbytestudio.data.network.ApiException
import com.flowbytestudio.data.network.NetworkException
import retrofit2.HttpException
import java.io.IOException

suspend inline fun <T> runCatchingApi(crossinline block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch(e: HttpException)
{
    Result.failure(ApiException(code = e.code(), errorMessage = e.message(), cause = e))
} catch(e: IOException)
{
    Result.failure(NetworkException(e))
} catch(e: Exception)
{
    Result.failure(e)
}