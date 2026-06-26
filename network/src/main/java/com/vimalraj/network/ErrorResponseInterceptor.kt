package com.vimalraj.network

import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * Intercepts HTTP responses and checks if the JSON contains error indicators
 * even when HTTP status is 200 OK.
 *
 * This is necessary because some servers return errors with 200 status code
 * but include error details in the response body (e.g., {"status": "UserException", "error": "..."})
 */
class ErrorResponseInterceptor : Interceptor {

    private val gson = Gson()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // Only check successful responses (200 OK)
        if (response.isSuccessful) {
            val responseBody = response.body
            val bodyString = responseBody?.string() ?: return response

            // Try to detect if this is an error response
            try {
                val errorResponse = gson.fromJson(bodyString, ErrorResponse::class.java)

                // Check if it contains error indicators
                if (errorResponse?.status?.contains("Exception", ignoreCase = true) == true ||
                    errorResponse?.error != null
                ) {
                    // Throw exception that will be caught by safeApiCall
                    throw ServerErrorException(
                        status = errorResponse.status,
                        error = errorResponse.error,
                        timestamp = errorResponse.timestamp
                    )
                }
            } catch (e: ServerErrorException) {
                // Re-throw our custom exception
                throw e
            } catch (e: Exception) {
                // If JSON parsing fails, it's not an error response, continue normally
            }

            // Recreate the response with the body (since we consumed it by reading it)
            val newResponseBody = bodyString.toResponseBody(responseBody.contentType())
            return response.newBuilder()
                .body(newResponseBody)
                .build()
        }

        return response
    }
}

