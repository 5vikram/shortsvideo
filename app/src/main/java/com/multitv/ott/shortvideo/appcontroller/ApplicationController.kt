package com.multitv.ott.shortvideo.appcontroller

import android.content.Context
import android.text.TextUtils
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.android.volley.*
import com.android.volley.toolbox.Volley
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.multitv.ott.shortvideo.utils.NukeSSLCerts
import com.multitv.ott.shortvideo.utils.Tracer

class ApplicationController : MultiDexApplication() {

    private val TAG = ApplicationController::class.java.simpleName
    private var mRequestQueue: RequestQueue? = null
    var screenWidth = 0
    var screenHeight = 0

    companion object {
        @get:Synchronized
        var instance: ApplicationController? = null
            private set

        lateinit var simpleCache: SimpleCache
        const val exoPlayerCacheSize: Long = 90 * 1024 * 1024
        lateinit var leastRecentlyUsedCacheEvictor: LeastRecentlyUsedCacheEvictor
        lateinit var exoDatabaseProvider: ExoDatabaseProvider
    }


    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
        NukeSSLCerts().nuke()
        instance = this

        val mDisplayMetrics = applicationContext.resources
            .displayMetrics
        screenWidth = mDisplayMetrics.widthPixels
        screenHeight = mDisplayMetrics.heightPixels


        leastRecentlyUsedCacheEvictor = LeastRecentlyUsedCacheEvictor(exoPlayerCacheSize)
        exoDatabaseProvider = ExoDatabaseProvider(this)
        simpleCache = SimpleCache(cacheDir, leastRecentlyUsedCacheEvictor, exoDatabaseProvider)
    }


    fun clearCache() {
        simpleCache.release()
    }

    fun removeKeyFromCache(key: String) {
        simpleCache.removeResource(key)
    }

    override fun attachBaseContext(base: Context?) {
        MultiDex.install(this)
        super.attachBaseContext(base)
    }

    val requestQueue: RequestQueue?
        get() {
            if (mRequestQueue == null) {
                mRequestQueue = Volley.newRequestQueue(applicationContext)
            }
            return mRequestQueue
        }

    fun <T> addToRequestQueue(req: Request<T>, tag: String?) {
        req.tag = if (TextUtils.isEmpty(tag)) TAG else tag
        requestQueue!!.add(req)
    }

    fun <T> addToRequestQueue(req: Request<T>) {
        req.tag = TAG
        Tracer.error(TAG, "AppController.addToRequestQueue() $req")
        requestQueue!!.add(req)
    }

    fun cancelPendingRequests(tag: Any?) {
        if (mRequestQueue != null) {
            mRequestQueue!!.cancelAll(tag)
        }
    }

}