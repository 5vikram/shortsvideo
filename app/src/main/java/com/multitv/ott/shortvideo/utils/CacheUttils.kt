package com.multitv.ott.shortvideo.utils

import com.google.gson.reflect.TypeToken
import com.multitv.ott.shortvideo.ContentItem
import com.multitv.ott.shortvideo.ShortVideo
import com.multitv.ott.shortvideo.appcontroller.ApplicationController
import java.util.*

object CacheUttils {
    fun getHomeCacheData(): ShortVideo {
        val key = "Home"
        val homeObjectType = object : TypeToken<ShortVideo?>() {}.type

        return if (Objects.requireNonNull(
                Objects.requireNonNull(ApplicationController.getInstance())
                    .getCacheManager()
            )?.get(
                key,
                ShortVideo::class.java, homeObjectType
            ) != null
        ) ApplicationController.getInstance().getCacheManager()?.get(
            key,
            ShortVideo::class.java, homeObjectType
        ) as ShortVideo else ShortVideo()
    }
}