package com.multitv.ott.export

import android.app.Application
import android.os.Bundle
import android.util.Log
import com.gyf.immersionbar.ktx.immersionBar
import com.volcengine.effectone.ui.BaseActivity
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors

/**
 * 导出页
 */
class EffectOneExportActivity : BaseActivity() {
    companion object {
        const val TAG = "EffectOneExportActivity"

        @JvmField
        var waterMarkPath: String? = null
        private val IOExecutors by lazy { Executors.newSingleThreadExecutor() }

        @JvmStatic
        fun copyWaterMark(app: Application) {
            //拷贝assets目录中watermark水印文件到沙盒目录中，导出视频加水印需要用到
            val waterMarkFile =
                File(app.getExternalFilesDir("waterMark"), "watermark.jpg")

            waterMarkFile.takeIf { it.exists().not() }?.let { targetPath ->
                val waterMarkAssets = "waterMark/watermark.png"
                IOExecutors.execute {
                    runCatching {
                        val input = app.assets.open(waterMarkAssets)
                        input.use {
                            input.copyTo(FileOutputStream(targetPath))
                        }
                        input.close()
                        waterMarkFile
                    }.getOrNull()?.let {
                        Log.d(TAG, "copyWaterMark() called  file ${it.path}")
                        waterMarkPath = waterMarkFile.absolutePath
                    }
                }
            } ?: run { waterMarkPath = waterMarkFile.absolutePath }

        }
    }

    private val effectOneExportFragment by lazy {
        EffectOneExportFragment().apply {
            arguments = intent.extras
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersionBar {
            statusBarColor(com.volcengine.effectone.editorui.R.color.BGPrimary)
            navigationBarColor(com.volcengine.effectone.editorui.R.color.BGPrimary)
            fitsSystemWindows(true)
            statusBarDarkFont(false)
        }

        val fragment = supportFragmentManager.findFragmentByTag(EffectOneExportFragment.TAG)
            ?: effectOneExportFragment
        supportFragmentManager
            .beginTransaction()
            .replace(android.R.id.content, fragment, EffectOneExportFragment.TAG)
            .commitNowAllowingStateLoss()
        //拷贝水印
        copyWaterMark(application)
    }
}