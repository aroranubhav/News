package com.maxi.news.framework.di.module

import android.content.Context
import android.util.Base64
import androidx.room.Room
import androidx.work.WorkManager
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.maxi.news.BuildConfig
import com.maxi.news.common.DefaultDispatcherProvider
import com.maxi.news.common.DefaultNetworkConnectivityHelper
import com.maxi.news.common.DispatcherProvider
import com.maxi.news.common.NetworkConnectivityHelper
import com.maxi.news.data.source.local.dao.NewsSourcesDao
import com.maxi.news.data.source.local.db.NewsDatabase
import com.maxi.news.data.source.remote.api.NetworkApiService
import com.maxi.news.data.source.remote.interceptor.AuthorizationInterceptor
import com.maxi.news.data.source.remote.interceptor.CacheControlInterceptor
import com.maxi.news.data.source.remote.interceptor.ErrorHandlingInterceptor
import com.maxi.news.data.source.remote.interceptor.HttpLoggingInterceptorFactory
import com.maxi.news.data.sync.BackgroundSyncScheduler
import com.maxi.news.framework.di.module.AppModule.Constants.BASE_URL
import com.maxi.news.framework.di.module.AppModule.Constants.CONNECTION_TIME_OUT
import com.maxi.news.framework.di.module.AppModule.Constants.NEWS_DATABASE
import com.maxi.news.framework.di.module.AppModule.Constants.OKHTTP_CACHE
import com.maxi.news.framework.di.module.AppModule.Constants.READ_TIME_OUT
import com.maxi.news.framework.di.qualifier.ApiKey
import com.maxi.news.framework.di.qualifier.BaseUrl
import com.maxi.news.framework.di.qualifier.IsDebug
import com.maxi.news.framework.di.qualifier.UserAgent
import com.maxi.news.framework.work.DefaultBackgroundSyncScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private object Constants {
        const val BASE_URL = "https://newsapi.org/v2/"
        const val OKHTTP_CACHE = "okhttp_cache"

        const val CONNECTION_TIME_OUT = 30L
        const val READ_TIME_OUT = 30L

        const val NEWS_DATABASE = "news_database"
    }

    @BaseUrl
    @Provides
    fun provideBaseUrl(): String =
        BASE_URL

    @ApiKey
    @Provides
    fun provideApiKey(): String =
        String(
            Base64
                .decode(
                    BuildConfig.API_KEY,
                    Base64.DEFAULT
                ),
            Charsets.UTF_8
        )

    @UserAgent
    @Provides
    fun provideUserAgent(): String =
        BuildConfig.USER_AGENT

    @IsDebug
    @Provides
    fun provideIsDebug(): Boolean =
        BuildConfig.DEBUG

    @Singleton
    @Provides
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }

    @Singleton
    @Provides
    fun provideCache(
        @ApplicationContext context: Context
    ): Cache =
        Cache(
            File(
                context.cacheDir,
                OKHTTP_CACHE
            ),
            10L * 1024 * 1024
        )

    @Singleton
    @Provides
    fun provideAuthorizationInterceptor(
        @ApiKey apiKey: String,
        @UserAgent userAgent: String
    ): AuthorizationInterceptor =
        AuthorizationInterceptor(apiKey, userAgent)

    @Singleton
    @Provides
    fun provideCacheControlInterceptor(): CacheControlInterceptor =
        CacheControlInterceptor()

    @Singleton
    @Provides
    fun provideErrorHandlingInterceptor(
        json: Json
    ): ErrorHandlingInterceptor =
        ErrorHandlingInterceptor(json)

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(
        @IsDebug isDebug: Boolean
    ): HttpLoggingInterceptor =
        HttpLoggingInterceptorFactory(isDebug)
            .create()

    @Singleton
    @Provides
    fun provideHttpClient(
        cache: Cache,
        authorizationInterceptor: AuthorizationInterceptor,
        cacheControlInterceptor: CacheControlInterceptor,
        errorHandlingInterceptor: ErrorHandlingInterceptor,
        httpLoggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient =
        OkHttpClient()
            .newBuilder()
            .cache(cache)
            .connectTimeout(CONNECTION_TIME_OUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIME_OUT, TimeUnit.SECONDS)
            .addInterceptor(authorizationInterceptor)
            .addInterceptor(errorHandlingInterceptor)
            .addNetworkInterceptor(cacheControlInterceptor)
            .addInterceptor(httpLoggingInterceptor)
            .build()

    @Singleton
    @Provides
    fun provideRetrofit(
        @BaseUrl baseUrl: String,
        httpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()

        return retrofit
    }

    @Singleton
    @Provides
    fun provideNetworkApiService(
        retrofit: Retrofit
    ): NetworkApiService =
        retrofit.create(NetworkApiService::class.java)

    @Singleton
    @Provides
    fun provideDispatcherProvider(): DispatcherProvider =
        DefaultDispatcherProvider()

    @Singleton
    @Provides
    fun provideNetworkConnectivityHelper(
        @ApplicationContext context: Context
    ): NetworkConnectivityHelper =
        DefaultNetworkConnectivityHelper(context)

    @Singleton
    @Provides
    fun provideNewsDatabase(
        @ApplicationContext context: Context
    ): NewsDatabase =
        Room.databaseBuilder(
            context,
            NewsDatabase::class.java,
            NEWS_DATABASE
        ).build()

    @Provides
    @Singleton
    fun provideNewsSourcesDao(
        database: NewsDatabase
    ): NewsSourcesDao =
        database.newsSourcesDao()

    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context
    ): WorkManager =
        WorkManager.getInstance(context)

    @Provides
    @Singleton
    fun provideBackgroundSyncScheduler(
        workManager: WorkManager,
        json: Json,
        dispatcherProvider: DispatcherProvider
    ): BackgroundSyncScheduler =
        DefaultBackgroundSyncScheduler(
            workManager,
            json,
            dispatcherProvider
        )

}