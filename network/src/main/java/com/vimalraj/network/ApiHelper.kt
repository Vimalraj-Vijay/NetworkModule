package com.vimalraj.network

import android.util.MalformedJsonException
import com.google.gson.JsonParseException
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.TimeoutException

// Safe API call handler with suspend
suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): ResultHandler<T> {
    return try {
        val response = apiCall()
        when {
            response.isSuccessful -> {
                response.body()?.let { ResultHandler.Success(it) }
                    ?: ResultHandler.Error(
                        message = "HTTP 200: Empty response body",
                        remoteApiError = RemoteApiError.EMPTY_BODY
                    )
            }

            else -> ResultHandler.Error(
                message = "HTTP Error: ${response.code()} - ${response.message()}",
                remoteApiError = RemoteApiError.UNEXPECTED_ERROR
            )
        }
    } catch (exception: Throwable) {
        exception.stackTrace
        handleApiError(exception)
    }
}

// Exception handling with detailed Resource.Error
private fun <T> handleApiError(exception: Throwable): ResultHandler<T> {
    val remoteApiError = when (exception) {
        is TimeoutException -> RemoteApiError.TIMEOUT
        is NoConnectivityException -> RemoteApiError.NO_INTERNET
        is IOException -> RemoteApiError.IO_ERROR
        is HttpException -> {
            when (exception.code()) {
                400 -> RemoteApiError.BAD_REQUEST
                401 -> RemoteApiError.UNAUTHORIZED
                403 -> RemoteApiError.FORBIDDEN_ACCESS_DENIED
                404 -> RemoteApiError.RESOURCE_NOT_FOUND
                500 -> RemoteApiError.SERVER_ERROR
                503 -> RemoteApiError.SERVICE_UNAVAILABLE
                else -> RemoteApiError.UNEXPECTED_HTTP_ERROR
            }
        }

        is JsonParseException, is MalformedJsonException -> RemoteApiError.JSON_PARSE
        is IllegalArgumentException -> RemoteApiError.ILLEGAL_ARGUMENT
        is IllegalStateException -> RemoteApiError.ILLEGAL_STATE
        else -> RemoteApiError.UNEXPECTED_ERROR
    }
    return ResultHandler.Error(
        message = exception.message.toString(),
        exception = exception,
        remoteApiError = remoteApiError
    )
}

fun accessDeniedError(): ResultHandler<Nothing> {
    return ResultHandler.AccessDenied
}