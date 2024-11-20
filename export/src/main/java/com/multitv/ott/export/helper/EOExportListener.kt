package com.volcengine.effectone.export.helper

import android.content.Intent
import com.volcengine.ck.logkit.LogKit
import com.volcengine.effectone.EffectOneSdk
import com.volcengine.effectone.editorui.external.impl.EOExportDefaultListener
import com.volcengine.effectone.export.viewmode.EffectOneExportRootUIViewModel
import com.volcengine.effectone.export.viewmode.ExportActionState
import kotlin.math.roundToInt

/**
 *Author: gaojin
 *Time: 2024/2/23 10:11
 *
 *  导出监听 中转一下成 ExportActionState
 *  @see ExportActionState
 */

open class EOExportListener(
    private var eoRootUIViewModel: EffectOneExportRootUIViewModel
) : EOExportDefaultListener() {

    override fun onDone(outputPath: String) {
        LogKit.d(TAG, "onCompileDone() outputPath  $outputPath")
        eoRootUIViewModel.exportActionState.postValue(ExportActionState.SUCCESS)
    }

    override fun onError(error: Int, msg: String?, ) {
        LogKit.d(TAG, "onCompileError() called with: error = $error, msg = $msg")
        eoRootUIViewModel.exportActionState.postValue(ExportActionState.FAIlED(error, msg))
    }

    override fun onProgress(progress: Float) {
        LogKit.d(TAG, "onCompileProgress() : progress = ${progress.times(100).roundToInt()}")
        eoRootUIViewModel.exportActionState.postValue(ExportActionState.LOADING(progress.times(100).roundToInt()))
    }

}

class ActivityResultExportListener(eoRootUIViewModel: EffectOneExportRootUIViewModel) :
    EOExportListener(eoRootUIViewModel) {
    companion object {
        const val RESULT_EXPORT_PATH = 10004
        const val KEY_OUTPUT_PATH = "KEY_OUTPUT_PATH"
    }

    override fun onDone(outputPath: String) {
        LogKit.d(TAG, "onCompileDone() outputPath  $outputPath")
        EffectOneSdk.activityManager.finishActivities(null, RESULT_EXPORT_PATH, Intent().apply {
            putExtra(KEY_OUTPUT_PATH, outputPath)
        })
    }
}