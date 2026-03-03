package com.maxi.news.data.source.remote.interceptor

import com.maxi.news.data.common.DataConstants
import com.maxi.news.data.common.DataConstants.Headers
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

class CacheControlInterceptor : Interceptor {

    companion object {
        private const val MAX_AGE_SECONDS = 86400 //1 day
        private const val MAX_STALE_SECONDS = 604800 //7 days
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        if (originalRequest.method != "GET") {
            return chain.proceed(originalRequest)
        }

        val forceRefresh = originalRequest.header(Headers.X_FORCE_REFRESH) == "true"

        val updatedRequest = originalRequest
            .newBuilder()
            .removeHeader(Headers.PRAGMA)
            .removeHeader(Headers.X_FORCE_REFRESH)
            .apply {
                if (forceRefresh) {
                    cacheControl(
                        CacheControl.FORCE_NETWORK
                    )
                } else {
                    cacheControl(
                        CacheControl.Builder()
                            .maxAge(MAX_AGE_SECONDS, TimeUnit.SECONDS)
                            .maxStale(MAX_STALE_SECONDS, TimeUnit.SECONDS)
                            .build()
                    )
                }
            }.build()

        val networkResponse = chain.proceed(updatedRequest)
        val cacheHeader = networkResponse.header(Headers.CACHE_CONTROL)

        val hasCacheSupport = cacheHeader != null && !cacheHeader.contains("no-cache") && !cacheHeader.contains("no-store")

        return networkResponse
            .newBuilder()
            .removeHeader(Headers.PRAGMA)
            .apply {
                if (!hasCacheSupport) {
                    header(Headers.CACHE_CONTROL, "public, max-age=$MAX_STALE_SECONDS")
                }
            }.build()
    }
}