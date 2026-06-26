package com.vimalraj.network

import com.google.gson.annotations.SerializedName

/**
 * Error response structure from the server
 * Used to detect when server returns 200 OK but with error details
 */
data class ErrorResponse(
    @SerializedName("status")
    val status: String? = null,

    @SerializedName("error")
    val error: String? = null,

    @SerializedName("timestamp")
    val timestamp: String? = null
)

