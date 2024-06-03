package com.multitv.ott.shortvideo.utils

import android.content.Context
import java.io.File
import java.io.IOException


object Uttils {
    val TOKEN = "token"
    val INVAILD_USER = "Authentication failed, Please contact with admin"

    fun getVideoCacheDir(context: Context): File {
        return File(context.externalCacheDir, "video-cache")
    }

    @Throws(IOException::class)
    fun cleanVideoCacheDir(context: Context) {
        val videoCacheDir = getVideoCacheDir(context)
        cleanDirectory(videoCacheDir)
    }

    @Throws(IOException::class)
    private fun cleanDirectory(file: File) {
        if (!file.exists()) {
            return
        }
        val contentFiles = file.listFiles()
        if (contentFiles != null) {
            for (contentFile in contentFiles) {
                delete(contentFile)
            }
        }
    }

    @Throws(IOException::class)
    private fun delete(file: File) {
        if (file.isFile && file.exists()) {
            deleteOrThrow(file)
        } else {
            cleanDirectory(file)
            deleteOrThrow(file)
        }
    }

    @Throws(IOException::class)
    private fun deleteOrThrow(file: File) {
        if (file.exists()) {
            val isDeleted = file.delete()
            if (!isDeleted) {
                throw IOException(String.format("File %s can't be deleted", file.absolutePath))
            }
        }
    }
}
