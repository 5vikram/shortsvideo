package com.volcengine.effectone.export.helper

import android.os.Environment
import com.volcengine.ck.logkit.LogKit
import com.volcengine.easyeditor.utils.FileUtil
import com.volcengine.effectone.export.viewmode.EffectOneExportRootUIViewModel
import com.volcengine.effectone.singleton.AppSingleton
import com.volcengine.effectone.utils.runOnUiThread
import com.volcengine.effectone.widget.EOToaster
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


open class EOExportWithUploadListener(eoRootUIViewModel: EffectOneExportRootUIViewModel) :
    EOExportListener(eoRootUIViewModel) {

    private var savePath: File? = null
    private var outputPath = ""
    private var compileWithUploadFinish = false
    private var compileFinish = false

    init {
        val dateFormat = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault())
        val exportFileName = "eo-upload-${dateFormat.format(Date())}.mp4"
        val dir =
            "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)}${File.separator}${
                AppSingleton.instance.applicationInfo.loadLabel(AppSingleton.instance.packageManager)
            }"
        savePath = File(dir, exportFileName)
    }

    /**
     * {zh}导出数据回调-用于边合成边上传场景
     * @param data 数据
     * @param offset 偏移量
     * @param size 数据大小
     * @param isFinish 是否结束
     */
    override fun onEncoderDataAvailable(
        data: ByteArray?,
        offset: Long,
        size: Int,
        isFinish: Boolean
    ) {
        //下方逻辑仅为展示接口如何使用,将回调数据写入到文件中, 根据实际业务场景对返回的数据做处理。
        if (data != null) {
            savePath?.let {
                LogKit.d(
                    TAG,
                    "EOExportWithUpload data:$data offset:$offset size:$size isFinish:$isFinish savePath:${savePath?.path}"
                )
                FileUtil.saveByteArrayToFile(data, offset, size, it)
            }
        } else {
            LogKit.e(
                TAG,
                "EOExportWithUpload param error data:$data offset:$offset size:$size isFinish:$isFinish savePath:${savePath?.path}",
                null
            )
        }

        if (!isFinish) {
            return
        }
        compileWithUploadFinish = true
        checkFileMD5()
    }

    override fun onDone(outputPath: String) {
        compileFinish = true
        this.outputPath = outputPath
        checkFileMD5()
        super.onDone(outputPath)
    }

    @Synchronized
    private fun checkFileMD5() {
        if (!compileFinish || !compileWithUploadFinish) {
            return
        }
        savePath?.let {
            val compileMD5 = FileUtil.getHash(it.absolutePath)
            val compileWithUploadMD5 = FileUtil.getHash(outputPath)
            val toastStr =
                if (compileMD5 == compileWithUploadMD5) "合成文件MD5一致" else "合成文件MD5不一致"

            runOnUiThread {
                EOToaster.show(AppSingleton.instance.applicationContext, toastStr)
            }
        }
    }


}