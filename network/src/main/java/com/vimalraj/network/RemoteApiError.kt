package com.vimalraj.network

enum class RemoteApiError(val code: Int = 0, val exceptionMessage: Int = 0) {
    // HTTP Error
    SERVER_ERROR(code = 500, exceptionMessage = R.string.server_error),
    SERVICE_UNAVAILABLE(code = 503, exceptionMessage = R.string.service_unavailable),
    BAD_REQUEST(code = 400, exceptionMessage = R.string.bad_request),
    UNAUTHORIZED(code = 401, exceptionMessage = R.string.unauthorized),
    FORBIDDEN_ACCESS_DENIED(code = 403, exceptionMessage = R.string.forbidden_access_denied),
    RESOURCE_NOT_FOUND(code = 404, exceptionMessage = R.string.resource_not_found),
    UNEXPECTED_HTTP_ERROR(exceptionMessage = R.string.unknown_http_error),

    // Other exceptionM
    TIMEOUT(exceptionMessage = R.string.timeout),
    NO_INTERNET(exceptionMessage = R.string.no_internet_connection),
    IO_ERROR(exceptionMessage = R.string.io_exception),
    JSON_PARSE(exceptionMessage = R.string.parse_failed),
    ILLEGAL_ARGUMENT(exceptionMessage = R.string.invalid_argument),
    ILLEGAL_STATE(exceptionMessage = R.string.illegal_application_state),
    UNEXPECTED_ERROR(exceptionMessage = R.string.unexpected_error),
    EMPTY_BODY(exceptionMessage = R.string.empty_response),

}