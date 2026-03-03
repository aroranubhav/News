package com.maxi.news.common

import java.io.IOException

open class ApiException(
    val errorCode: Int,
    val errorMessage: String?,
    val errorBody: String?,
    val requestMethod: String?,
    val requestUrl: String?
) : IOException() {

    override val message: String
        get() = buildString {
            append("HTTP Exception: $errorCode")

            if (!errorMessage.isNullOrEmpty()) {
                append(" -- $errorMessage\n")
            }

            if (!requestMethod.isNullOrEmpty() && !requestUrl.isNullOrEmpty()) {
                append("[$requestMethod -- $requestUrl]\n")
            }

            append(errorBody)
        }
}

sealed class HttpException(
    errorCode: Int,
    errorMessage: String?,
    errorBody: String?,
    requestMethod: String?,
    requestUrl: String?
) : ApiException(
    errorCode, errorMessage, errorBody, requestMethod, requestUrl
) {

    class Unauthorized(
        errorMessage: String?, errorBody: String?, requestMethod: String?, requestUrl: String?
    ) : HttpException(401, errorMessage, errorBody, requestMethod, requestUrl)

    class Forbidden(
        errorMessage: String?, errorBody: String?, requestMethod: String?, requestUrl: String?
    ) : HttpException(403, errorMessage, errorBody, requestMethod, requestUrl)

    class NotFound(
        errorMessage: String?, errorBody: String?, requestMethod: String?, requestUrl: String?
    ) : HttpException(404, errorMessage, errorBody, requestMethod, requestUrl)

    class ServerError(
        errorCode: Int, errorMessage: String?, errorBody: String?, requestMethod: String?, requestUrl: String?
    ) : HttpException(errorCode, errorMessage, errorBody, requestMethod, requestUrl)

    class Unknown(
        errorCode: Int, errorMessage: String?, errorBody: String?, requestMethod: String?, requestUrl: String?
    ) : HttpException(errorCode, errorMessage, errorBody, requestMethod, requestUrl)
}