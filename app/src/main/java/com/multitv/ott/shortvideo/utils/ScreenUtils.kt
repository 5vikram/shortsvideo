package com.multitv.ott.shortvideo.utils

import android.content.Context
import android.graphics.Point
import android.view.WindowManager
import android.util.DisplayMetrics



object ScreenUtils {

    fun getScreenWidth(context: Context): Int {
        var screenWidth = 0

        try {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wm.defaultDisplay
            val size = Point()
            display.getSize(size)
            screenWidth = size.x
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return screenWidth
    }

    fun getScreenHeight(context: Context): Int {
        var screenHeight = 0

        try {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wm.defaultDisplay
            val size = Point()
            display.getSize(size)
            screenHeight = size.y
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return screenHeight
    }
    fun convertPixelsToDp(px: Int, context: Context): Int {
        return px / (context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
    }

}
