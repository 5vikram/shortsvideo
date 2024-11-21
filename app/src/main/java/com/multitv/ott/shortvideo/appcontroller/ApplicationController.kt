package com.multitv.ott.shortvideo.appcontroller

import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.multidex.BuildConfig
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.android.volley.*
import com.android.volley.toolbox.Volley
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.iainconnor.objectcache.CacheManager
import com.iainconnor.objectcache.DiskCache
import com.multitv.ott.shortvideo.EOQuickInitHelper
import com.multitv.ott.shortvideo.utils.NukeSSLCerts
import com.multitv.ott.shortvideo.utils.Tracer
import com.multitv.ott.shortvideo.utils.Uttils
import java.io.File
import java.util.Objects

class ApplicationController : MultiDexApplication() {

    private val TAG = ApplicationController::class.java.simpleName
    private var mRequestQueue: RequestQueue? = null
    var screenWidth = 0
    var screenHeight = 0

    private var cacheManager: CacheManager? = null
    private lateinit var diskCache: DiskCache

    fun getCacheManager(): CacheManager? {
        if (cacheManager == null) {
            try {
                val cacheFile = File(filesDir.toString() + File.separator + packageName)
                diskCache = DiskCache(cacheFile, BuildConfig.VERSION_CODE, 1024 * 1024 * 30)
                cacheManager = CacheManager.getInstance(diskCache)
            } catch (e: Exception) {
                Log.e("cacheManager", "" + e.message)
            }
        }
        return cacheManager
    }

    fun refershHomeData() {
        val key = "Home"
        Objects.requireNonNull(
            Objects.requireNonNull(getInstance())
                .getCacheManager()
        )?.put(key, null)
    }



    companion object {

        private var instance: ApplicationController? = null

        fun getInstance(): ApplicationController {
            return instance ?: throw IllegalStateException("ApplicationController not initialized!")
        }

        lateinit var simpleCache: SimpleCache
        const val exoPlayerCacheSize: Long = 100 * 1024 * 1024
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

        EOQuickInitHelper.licenseFileName =
            "multitv1_test_20241029_20241129_com.multitv.ott.shortvideo_1.5.0_212.licbag" //Replace this with your own license name.
        EOQuickInitHelper.initApplication(this)
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