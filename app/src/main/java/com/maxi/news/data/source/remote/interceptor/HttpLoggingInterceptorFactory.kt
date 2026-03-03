package com.maxi.news.data.source.remote.interceptor

import okhttp3.logging.HttpLoggingInterceptor

class HttpLoggingInterceptorFactory(
    private val isDebug: Boolean
) {

    fun create(): HttpLoggingInterceptor =
        HttpLoggingInterceptor()
            .apply {
                level = if (isDebug) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            }
}