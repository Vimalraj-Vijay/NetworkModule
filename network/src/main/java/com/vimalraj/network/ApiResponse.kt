package com.vimalraj.network

import com.google.gson.annotations.SerializedName

/**
 * Generic API response wrapper for all API endpoints
 *
 * Success responses vary by endpoint (generic type T)
 * Failure responses always have the same structure (success, message, timestamp)
 *
 * Usage examples:
 * - ApiResponse<AuthResponse> for login/register
 * - ApiResponse<UserProfile> for profile APIs
 * - ApiResponse<List<Product>> for product listing
 * - ApiResponse<Unit> for APIs with no data response
 */
data class ApiResponse<T>(
    // Success/failure indicator
    @SerializedName("success")
    val success: Boolean? = null,

    // Message field (present in all responses)
    @SerializedName("message")
    val message: String? = null,

    // Timestamp field (present in all responses)
    @SerializedName("timestamp")
    val timestamp: String? = null,

    // Success response data (generic - varies by API)
    @SerializedName("data")
    val data: T? = null
) {
    /**
     * Check if this is a failure response
     * Failure is identified by success == false or data == null
     */
    fun isFailure(): Boolean {
        return success != true || data == null
    }

    /**
     * Check if this is a success response
     * Success requires success == true AND data != null
     */
    fun isSuccess(): Boolean = success == true && data != null

    /**
     * Get error message or null
     */
    fun getErrorMessage(): String? = message
}

/**
 * Extension function to convert ApiResponse to ResultHandler
 * Success: success == true AND data != null
 * Error: success != true OR data == null
 */
fun <T> ApiResponse<T>.toResultHandler(): ResultHandler<T> {
    return if (isSuccess()) {
        // success == true AND data != null
        ResultHandler.Success(data!!)
    } else {
        // success != true OR data == null
        ResultHandler.Error(
            message = message ?: "Unknown error occurred",
            remoteApiError = RemoteApiError.BAD_REQUEST
        )
    }
}

