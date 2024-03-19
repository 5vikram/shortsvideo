package com.multitv.ott.shortvideo.uttls

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.multitv.ott.shortvideo.R

import com.multitv.ott.shortvideo.utils.Tracer
import java.io.InputStream
import java.net.*

class CommonUtils {

    fun isDrmContent(videoUrl: String): Boolean {
        return videoUrl.split("\\.".toRegex())[1] == PlayerConstant.MPD
    }

    fun isAppIsInBackground(context: Context): Boolean {
        var isInBackground = true
        try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningProcesses = am.runningAppProcesses
            for (processInfo in runningProcesses) {
                if (processInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (activeProcess in processInfo.pkgList) {
                        if (activeProcess == context.packageName) {
                            isInBackground = false
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return isInBackground
    }

    fun setDefaultCookieManager() {
        val defaultCookieManager = CookieManager()
        defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER)
        val currentHandler = CookieHandler.getDefault()
        if (currentHandler !== defaultCookieManager) {
            CookieHandler.setDefault(defaultCookieManager)
        }
    }

    fun downloadImage(path: String?): Bitmap? {
        var `in`: InputStream? = null
        var bmp: Bitmap? = null
        var responseCode = -1
        try {
            val url = URL(path)
            val con = url.openConnection() as HttpURLConnection
            con.doInput = true
            con.connect()
            responseCode = con.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                //download
                `in` = con.inputStream
                bmp = BitmapFactory.decodeStream(`in`)
                `in`.close()
            }
        } catch (ex: java.lang.Exception) {
            Tracer.error("Exception:::", ex.toString())
        }
        return bmp
    }

    /* if return true then use dash.json file else use hls.json*/
    fun getMimeTypeFromExtension(videoUrl: String): Boolean {
        return getUrlExtension(videoUrl).equals("mpd", ignoreCase = true)
    }

    private fun getUrlExtension(url: String): String {
        return url.substring(url.lastIndexOf(".") + 1)
    }


    fun shareIntent(body: String, context: Context?) {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, body)
        context?.startActivity(
            Intent.createChooser(
                shareIntent,
                context.getString(R.string.send_to)
            )
        )
    }

}