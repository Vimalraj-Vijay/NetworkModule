package com.vimalraj.network

sealed class ResultHandler<out RESPONSE> {

    data class Success<RESPONSE>(val data: RESPONSE) : ResultHandler<RESPONSE>()

    data class Error(
        val message: String = "",
        val exception: Throwable? = null,
        val remoteApiError: RemoteApiError
    ) :
        ResultHandler<Nothing>()

    data object AccessDenied : ResultHandler<Nothing>()
}