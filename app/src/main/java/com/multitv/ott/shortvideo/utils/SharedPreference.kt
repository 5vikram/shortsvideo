package com.multitv.ott.shortvideo.utils
import android.content.Context
import android.net.Uri
import android.os.AsyncTask

/**
 * Created by Lenovo on 03-02-2017.
 */
class SharedPreference {
    var PREFS_NAME = "Channel4"


    fun setPreferenceString(context: Context?, key: String?, value: String?) {
        if (context == null) return

        val editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun setPreferenceBoolean(context: Context?, key: String?, value: Boolean) {
        if (context == null) return
        val editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getPreferenceString(context: Context, key: String?): String? {
        if (context == null) return null
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(key, "")
    }

    fun setPreferenceInt(context: Context?, key: String?, value: Int) {
        if (context == null) return
        val editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getPreferencesInt(context: Context?, key: String?): Int {
        if (context == null) return 0
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(key, 0)
    }

    fun getPreferenceBoolean(_context: Context?, key: String?): Boolean {
        if (_context == null) return false
        val prefs = _context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(key, false)
    }

}