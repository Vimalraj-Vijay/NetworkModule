package com.vimalraj.network

/**
 * Exception thrown when server returns 200 OK but with error structure
 * This happens when the server sends error details in the response body
 * instead of using proper HTTP error codes
 */
class ServerErrorException(
    val status: String?,
    val error: String?,
    val timestamp: String?
) : Exception(error ?: "Server returned an error")

