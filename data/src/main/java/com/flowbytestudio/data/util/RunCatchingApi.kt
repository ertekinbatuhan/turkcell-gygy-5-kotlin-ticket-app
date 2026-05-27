package com.flowbytestudio.data.util


import com.flowbytestudio.data.network.ApiException
import com.flowbytestudio.data.network.NetworkException
import retrofit2.HttpException
import java.io.IOException

suspend inline fun <T> runCatchingApi(crossinline block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch(e: HttpException)
{
    val errorBodyStr = try {
        e.response()?.errorBody()?.string()
    } catch (ex: Exception) {
        null
    }
    val errorMessage = if (!errorBodyStr.isNullOrBlank()) errorBodyStr else e.message()
    Result.failure(ApiException(code = e.code(), errorMessage = errorMessage, cause = e))
} catch(e: IOException)
{
    Result.failure(NetworkException(e))
} catch(e: Exception)
{
    Result.failure(e)
}