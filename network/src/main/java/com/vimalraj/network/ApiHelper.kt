package com.vimalraj.network

import android.util.MalformedJsonException
import com.google.gson.Gson
import com.google.gson.JsonParseException
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.HttpURLConnection
import java.util.concurrent.TimeoutException


/**
 * Generic safe API call for ApiResponse wrapped endpoints
 * Works with any response type T
 *
 * Success condition: response.isSuccessful && success == true && data != null
 * Error condition: any other case
 *
 * @param T The type of data in the success response
 * @param apiCall Suspend function that returns Response<ApiResponse<T>>
 * @return ResultHandler<T> with the unwrapped data or error
 */
suspend fun <T> safeApiCallWithWrapper(
    apiCall: suspend () -> Response<ApiResponse<T>>
): ResultHandler<T> {
    return try {
        val response = apiCall()

        if (response.isSuccessful) {
            val body = response.body()

            if (body != null) {
                // Check success field and data field
                if (body.success == true && body.data != null) {
                    ResultHandler.Success(body.data)
                } else if (body.success == true && body.data == null) {
                    ResultHandler.Partial(body.data, message = body.message ?: "Partial success with no data")
                } else {
                    ResultHandler.Error(
                        message = body.message ?: "Request failed",
                        remoteApiError = RemoteApiError.BAD_REQUEST
                    )
                }
            } else {
                ResultHandler.Error(
                    message = "HTTP ${response.code()}: Empty response body",
                    remoteApiError = RemoteApiError.EMPTY_BODY
                )
            }
        } else {
            // Try to parse error body as ApiResponse
            val errorBody = response.errorBody()?.string()
            val errorMessage = if (errorBody != null) {
                try {
                    val errorResponse = Gson().fromJson(errorBody, ApiResponse::class.java)
                    errorResponse.message ?: "HTTP Error: ${response.code()}"
                } catch (e: Exception) {
                    "HTTP Error: ${response.code()} - ${response.message()}"
                }
            } else {
                "HTTP Error: ${response.code()} - ${response.message()}"
            }

            ResultHandler.Error(
                message = errorMessage,
                remoteApiError = RemoteApiError.UNEXPECTED_ERROR
            )
        }
    } catch (exception: Throwable) {
        handleApiError(exception)
    } as ResultHandler<T>
}

// Exception handling with detailed Resource.Error
private fun <T> handleApiError(exception: Throwable): ResultHandler<T> {
    println("🚨 Exception caught in Network Call 🚨")
    exception.printStackTrace()
    val remoteApiError = when (exception) {
        is TimeoutException -> RemoteApiError.TIMEOUT
        is NoConnectivityException -> RemoteApiError.NO_INTERNET
        is JsonParseException, is MalformedJsonException -> RemoteApiError.JSON_PARSE
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
