package com.maxi.news.data.common

object DataConstants {

    object EndPoints {

        const val SOURCES = "top-headlines/sources"
        const val TOP_HEADLINES = "top-headlines"
    }

    object Keys {

        const val ID = "id"
        const val STATUS = "status"
        const val SOURCES = "sources"
        const val NAME = "name"
        const val DESCRIPTION = "description"
        const val URL = "url"
    }

    object Headers {

        const val X_FORCE_REFRESH = "x-force-refresh"
        const val X_API_KEY = "x-api-key"
        const val USER_AGENT = "User-Agent"
        const val CACHE_CONTROL = "Cache-Control"
        const val PRAGMA = "Pragma"
    }

    object Tables {

        const val NEWS_SOURCES = "news_sources"
    }

}