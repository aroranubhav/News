package com.maxi.news.data.source.remote.interceptor

import com.maxi.news.common.HttpException
import com.maxi.news.common.TransportException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

data class ErrorResponse(
    val error: String? = null,
    val message: String? = null
)

class ErrorHandlingInterceptor(
    private val json: Json
) : Interceptor {

    companion object {
        private const val MAX_ERROR_BODY_BYTES = 64 * 1024L
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val networkResponse: Response

        try {
            networkResponse = chain.proceed(originalRequest)
        } catch (e: UnknownHostException) {
            throw TransportException.NoConnectivity(e)
        } catch (e: ConnectException) {
            throw TransportException.NoConnectivity(e)
        } catch (e: SocketTimeoutException) {
            throw TransportException.Timeout(e)
        } catch (e: IOException) {
            throw TransportException.Unknown(e)
        }

        if (!networkResponse.isSuccessful) {
            val errorBody = networkResponse.peekBody(MAX_ERROR_BODY_BYTES).string()
            val errorCode = networkResponse.code

            val parsedError = try {
                json.decodeFromString<ErrorResponse>(errorBody)
            } catch (e: Exception) {
                null
            }

            val errorMessage = parsedError?.let {
                it.error ?: it.message
            } ?: networkResponse.message.takeIf {
                it.isNotBlank()
            } ?: "Unknown Error $errorCode"

            val requestMethod = originalRequest.method
            val requestUrl = originalRequest.url.toString()

            when (errorCode) {
                401 -> {
                    throw HttpException.Unauthorized(
                        errorMessage, errorBody, requestMethod, requestUrl
                    )
                }

                403 -> {
                    throw HttpException.Forbidden(
                        errorMessage, errorBody, requestMethod, requestUrl
                    )
                }

                404 -> {
                    throw HttpException.NotFound(
                        errorMessage, errorBody, requestMethod, requestUrl
                    )
                }

                in 500..599 -> {
                    throw HttpException.ServerError(
                        errorCode, errorMessage, errorBody, requestMethod, requestUrl
                    )
                }

                else -> {
                    throw HttpException.Unknown(
                        errorCode, errorMessage, errorBody, requestMethod, requestUrl
                    )
                }
            }
        }

        return networkResponse
    }
}