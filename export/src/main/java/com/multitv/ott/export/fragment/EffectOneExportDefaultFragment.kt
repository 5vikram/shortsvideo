package com.volcengine.effectone.export.fragment

import android.Manifest
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.preference.PreferenceManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.Group
import androidx.core.content.res.ResourcesCompat
import com.multitv.ott.export.R
import com.volcengine.ck.album.AlbumEntrance
import com.volcengine.ck.logkit.LogKit
import com.volcengine.effectone.editorui.external.data.EOOutputVideoSettings
import com.volcengine.effectone.editorui.external.data.EOResolution
import com.volcengine.effectone.editorui.external.impl.EOExportDefaultListener
import com.volcengine.effectone.export.base.EffectOneBaseFragment
import com.volcengine.effectone.export.data.PageScenes
import com.volcengine.effectone.export.data.isVisible
import com.volcengine.effectone.export.helper.ActivityResultExportListener
import com.volcengine.effectone.export.helper.EOExportListener
import com.volcengine.effectone.export.helper.EOExportWithUploadListener
import com.volcengine.effectone.export.viewmode.ActionEvent
import com.volcengine.effectone.export.viewmode.EffectOneExportRootUIViewModel
import com.volcengine.effectone.export.viewmode.EffectOneExportViewModel
import com.volcengine.effectone.export.viewmode.EffectOneVideoEncodeViewModel
import com.volcengine.effectone.export.viewmode.ExportActionState
import com.volcengine.effectone.export.viewmode.ExportEventTrackViewModel
import com.volcengine.effectone.extensions.setNoDoubleClickListener
import com.volcengine.effectone.singleton.AppSingleton
import com.volcengine.effectone.utils.EOUtils
import com.volcengine.effectone.utils.SizeUtil
import com.volcengine.effectone.widget.EOCommonDialog
import com.volcengine.effectone.widget.EOToaster
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt

/**
 * 默认UI控制，
 * @see PageScenes.DEFAULT
 * @see doExportAction 导出处理
 * @see backAction 物理返回和返回按钮逻辑
 */
class EffectOneExportDefaultFragment :
    EffectOneBaseFragment() {
    companion object {
        const val TAG = "EffectOneExportActionFragment"
        const val FIX_RATIO_SIZE = 16F / 9F
    }


    private val eoExportViewModel by lazy { EffectOneExportViewModel.get(requireActivity()) }
    private val eoVideoEncodeViewModel by lazy { EffectOneVideoEncodeViewModel.get(requireActivity()) }
    private val eoRootUIViewModel by lazy { EffectOneExportRootUIViewModel.get(requireActivity()) }
    private val eventTrackViewModel by lazy { ExportEventTrackViewModel.get(requireActivity()) }

    private val exportCoverEditor by lazy { rootView.findViewById<TextView>(R.id.export_cover_editor) }
    private val exportResolution by lazy { rootView.findViewById<TextView>(R.id.export_resolution) }
    private val exportAction by lazy { rootView.findViewById<TextView>(R.id.export_action) }

    private val exportBottomDefaultTop by lazy { rootView.findViewById<Group>(R.id.export_cover_editor_top_group) }

    //导出中标记
    private val isExporting = AtomicBoolean(false)

    //取消导出弹窗
    private var exportCancelDialog: EOCommonDialog? = null
    override val layoutResourceId: Int
        get() = R.layout.eo_main_fragment_export_bottom_default

    //导出后是否关闭页面，如果是，结果通过ActivityResult返回给EffectOneMainActivity.onActivityResult
    private var finishAfterExport = false

    override fun getFragmentTag() = tag ?: TAG
    override fun initView() {
        //调整drawableTop大小
        exportResolution?.setCompoundDrawables(
            null,
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.eo_export_resolution_540,
                requireActivity().theme
            )?.apply {
                setBounds(0, 0, SizeUtil.dp2px(28f), SizeUtil.dp2px(28f))
            },
            null,
            null
        )
        //调整drawableTop大小
        val coverIconDrawable = if (eoExportViewModel.eoExportManager.checkPhotoEditingMode()) {
            exportCoverEditor.alpha = 0.3f
            R.drawable.eo_export_cover_editor
        } else {
            exportCoverEditor.alpha = 1.0f
            R.drawable.eo_export_cover_editor
        }
        exportCoverEditor?.setCompoundDrawables(
            null,
            ResourcesCompat.getDrawable(resources, coverIconDrawable, requireActivity().theme)
                ?.apply {
                    setBounds(0, 0, SizeUtil.dp2px(28f), SizeUtil.dp2px(28f))
                },
            null,
            null
        )
    }

    override fun initObserver() {
        eventTrackViewModel.initExportObserver(viewLifecycleOwner)
        //注册物理返回监听
        requireActivity().onBackPressedDispatcher.addCallback(onBackPressedCallback)
        //点击编辑封图按钮
        exportCoverEditor.setNoDoubleClickListener {
            if (eoExportViewModel.eoExportManager.checkPhotoEditingMode()) return@setNoDoubleClickListener
            //尝试inflate stub
            eoRootUIViewModel.showCoverEditorView.value = true
        }
        //点击分辨率设置按钮
        exportResolution.setNoDoubleClickListener {
            EffectOneExportResolutionFragment.show(requireActivity())
        }
        //点击导出按钮
        exportAction.setNoDoubleClickListener {
            EOUtils.permission.checkPermissions(requireActivity(), albumPermissions, {
                //尝试inflate stub
                eoRootUIViewModel.showExportProgressView.value = true
            }, failedAction = { deniedList ->
                deniedList.firstOrNull()?.let {
                    AlbumEntrance.showAlbumPermissionTips(requireActivity())
                }
            })
        }
        //注册分辨fps变化监听
        eoVideoEncodeViewModel.resolutionFps.observe(viewLifecycleOwner) {
            it?.let {
                exportResolution.apply {
                    setCompoundDrawables(
                        null,
                        ResourcesCompat.getDrawable(
                            resources,
                            when (it.first) {
                                EOResolution.RES_540P -> R.drawable.eo_export_resolution_540
                                EOResolution.RES_720P -> R.drawable.eo_export_resolution_720
                                EOResolution.RES_1080P -> R.drawable.eo_export_resolution_1080
                                EOResolution.RES_4K -> R.drawable.eo_export_resolution_4k
                            }, requireActivity().theme
                        )?.apply {
                            setBounds(0, 0, SizeUtil.dp2px(28f), SizeUtil.dp2px(28f))
                        },
                        null,
                        null,
                    )
                }
            }
        }
        //注册页面变化监听
        eoRootUIViewModel.pageSense.observe(viewLifecycleOwner) { pageScenes ->
            when (pageScenes) {
                PageScenes.DEFAULT -> {
                    //显示默认UI
                    rootView.isVisible = true
                    exportBottomDefaultTop.isVisible = true
                    exportAction.isEnabled = true
                }

                PageScenes.COVER_EDITOR -> {
                    //隐藏默认UI
                    rootView.isVisible = false
                }

                PageScenes.EXPORT -> {
                    //显示默认UI
                    rootView.isVisible = true
                    //隐藏封图编辑和分辨率设置控件
                    exportBottomDefaultTop.isVisible = false
                    //导出按钮置灰
                    exportAction.isEnabled = false
                }

                else -> {}
            }
        }

        //监听用户点击事件
        eoRootUIViewModel.actionEvent.observe(viewLifecycleOwner) { action ->
            when (action) {
                //用户点击导出按钮
                ActionEvent.EXPORT_ACTION -> {
                    doExportAction()
                }
                //用户点击取消导出按钮
                ActionEvent.EXPORT_CANCEL -> {
                    backAction()
                }

                //用户点击左上角返回按钮
                ActionEvent.DEFAULT_BACK -> {
                    existPage()
                }

                else -> {}
            }
        }
        eoRootUIViewModel.exportActionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ExportActionState.FAIlED -> {
                    dismissExportDialog()
                    EOToaster.show(
                        AppSingleton.instance,
                        AppSingleton.instance.getString(R.string.eo_export_save_error)
                    )
                    eoRootUIViewModel.changeScenes(PageScenes.DEFAULT)
                }

                ExportActionState.SUCCESS -> {
                    dismissExportDialog()
                    EOToaster.show(
                        AppSingleton.instance,
                        AppSingleton.instance.getString(R.string.eo_export_cover_savetolocal)
                    )
                    eoRootUIViewModel.changeScenes(PageScenes.DEFAULT)
                }

                else -> {}
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        onBackPressedCallback.remove()
    }

    private val albumPermissions = mutableListOf<String>().apply {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            //Android 11（包含）不能通过权限使用外部非共享目录，需要使用MediaStore或者Intent方式提供Uri
        }
    }

    /**
     * 导出逻辑
     */
    private fun doExportAction() {
        eoRootUIViewModel.changeScenes(PageScenes.EXPORT)
        if (isExporting.compareAndSet(false, true)) {
            if (eoExportViewModel.eoExportManager.checkPhotoEditingMode()) {
                exportImage()
            } else {
                //exportWithUpload的判断根据业务实际情况进行判断处理是否使用边导出边上传
                val sp = PreferenceManager.getDefaultSharedPreferences(context)
                val exportWithUpload = sp.getBoolean("eo_export_with_upload", false)
                if (exportWithUpload) {
                    advancedExportVideo()
                } else {
                    exportVideo()
                }
            }
        }
    }

    // 导出图片
    private fun exportImage() {
        // 创建临时视频文件和导出图片文件
        val dateFormat = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault())

        val exportTempFileName = "eo-${dateFormat.format(Date())}.mp4"
        val tempFileDir = EOUtils.pathUtil.externalDir("temp_export").absolutePath
        val tempPath = File(tempFileDir, exportTempFileName)

        val exportFileName = "eo-${dateFormat.format(Date())}.jpg"
        val photoFileDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)?.toString()
        val photoPath = File(photoFileDir, exportFileName)

        //执行导出视频
        eoExportViewModel.eoExportManager.exportVideo(
            outputPath = tempPath.absolutePath,
            outputSetting = EOOutputVideoSettings().apply {
                //导出配置设置
                eoVideoEncodeViewModel.resolutionFps.value?.let {
                    outputRes = it.first
                    outputFps = it.second
                }
                //适配低端机或者导出失败机型，默认开启，使用软解方式
                useSoftwareDecode = true
                //硬编码，默认开启
                useHWEncoder = true

            },
            exportListener = EOExportImageListener(eoRootUIViewModel, photoPath, finishAfterExport)
        )
        eventTrackViewModel.exportImageEvent(photoPath.absolutePath, 1)
    }

    // 导出视频
    private fun exportVideo() {
        // 创建视频文件
        val dateFormat = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault())
        val exportFileName = "eo-${dateFormat.format(Date())}.mp4"
        val dir =
            "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)}${File.separator}${
                AppSingleton.instance.applicationInfo.loadLabel(AppSingleton.instance.packageManager)
            }"
        val savePath = File(dir, exportFileName)

        //执行导出视频
        eoExportViewModel.eoExportManager.exportVideo(
            outputPath = savePath.absolutePath,
            outputSetting = EOOutputVideoSettings().apply {
                //导出配置设置
                eoVideoEncodeViewModel.resolutionFps.value?.let {
                    outputRes = it.first
                    outputFps = it.second
                }
                //适配低端机或者导出失败机型，默认开启，使用软解方式
                useSoftwareDecode = true
                //硬编码，默认开启
                useHWEncoder = true

            },
            exportListener = if (finishAfterExport) {
                ActivityResultExportListener(eoRootUIViewModel)
            } else {
                EOExportListener(eoRootUIViewModel)
            }
        )
        eventTrackViewModel.exportVideoEvent(savePath.absolutePath, 1)

        Toast.makeText(requireActivity(), "path::" + savePath.absolutePath, Toast.LENGTH_SHORT).show()
    }


    // 高级导出视频接口
    private fun advancedExportVideo() {
        // 创建视频文件
        val dateFormat = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault())
        val exportFileName = "eo-${dateFormat.format(Date())}.mp4"
        val dir =
            "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)}${File.separator}${
                AppSingleton.instance.applicationInfo.loadLabel(AppSingleton.instance.packageManager)
            }"
        val savePath = File(dir, exportFileName)

        //初始化默认基础导出配置
        val outputSetting = EOOutputVideoSettings().apply {
            //导出配置设置
            eoVideoEncodeViewModel.resolutionFps.value?.let {
                outputRes = it.first
                outputFps = it.second
            }
            //适配低端机或者导出失败机型，默认开启，使用软解方式
            useSoftwareDecode = true
            //硬编码，默认开启
            useHWEncoder = true
        }

        //初始化导出扩展高级配置项
        val nleVideoEncodeSettings =
            eoExportViewModel.eoExportManager.eoExporter.transformDefaultVideoEncodeSettings(
                outputSetting
            ).apply {
                //开启边合成边导出
                enableUploadingWhileCompile = true
            }

        //执行导出视频
        eoExportViewModel.eoExportManager.exportVideoWithNleSetting(
            outputPath = savePath.absolutePath,
            nleVideoEncodeSettings = nleVideoEncodeSettings,
            exportListener = EOExportWithUploadListener(eoRootUIViewModel)
        )
//        eoExportViewModel.eoExportManager.cancelExport()
//        FileUtil.deleteFile(savePath.absolutePath)
        eventTrackViewModel.exportVideoEvent(savePath.absolutePath, 1)

        Toast.makeText(requireActivity(), "path::" + savePath.absolutePath, Toast.LENGTH_SHORT).show()
    }

    //隐藏Dialog，重置参数
    private fun dismissExportDialog() {
        exportCancelDialog.takeIf { isExporting.get() && it?.isShowing == true }?.dismiss()
        isExporting.set(false)
        exportCancelDialog = null
    }

    /**
     * 物理返回处理
     * @see ExportActionState
     */
    private val onBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                //根据当前PageScenes
                when (eoRootUIViewModel.pageSense.value) {
                    PageScenes.COVER_EDITOR -> {
                        val nextScenes = PageScenes.DEFAULT
                        eoRootUIViewModel.changeScenes(nextScenes)
                    }
                    //其他走导出check逻辑
                    else -> backAction()
                }
            }
        }

    private fun backAction() {
        if (isExporting.get()) {
            onBackPressedCallback.isEnabled = false
            exportCancelDialog = EOCommonDialog.Builder(requireActivity())
                .setTitle(R.string.eo_export_exit_title)
                .setContent(R.string.eo_export_exit_message)
                .setConfirmText(R.string.eo_export_confirm)
                .setCancelText(R.string.eo_export_cancel)
                .setConfirmListener(object :
                    EOCommonDialog.OnConfirmListener {
                    override fun onClick() {
                        eoExportViewModel.eoExportManager.takeIf {
                            isExporting.get()
                        }?.let {
                            //取消导出
                            eoExportViewModel.eoExportManager.cancelExport()
                            EOToaster.show(
                                AppSingleton.instance,
                                AppSingleton.instance.getString(R.string.eo_export_exit_export_tip)
                            )
                            //显示默认ui
                            eoRootUIViewModel.changeScenes(PageScenes.DEFAULT)
                            isExporting.set(false)
                        }
                    }
                })
                .show().apply {
                    //禁用物理返回取消
                    setCancelable(false)
                    setOnDismissListener {
                        onBackPressedCallback.isEnabled = true
                    }
                }

        } else {
            existPage()
        }
    }

    private fun existPage() {
        //退出导出页
        onBackPressedCallback.isEnabled = false
        activity?.onBackPressed()
    }

}

class EOExportImageListener(
    private val eoRootUIViewModel: EffectOneExportRootUIViewModel,
    private val photoPath: File,
    private val finishAfterExport: Boolean
) : EOExportDefaultListener() {

    override fun onDone(outputPath: String) {
        eoRootUIViewModel.exportActionState.postValue(ExportActionState.LOADING(100))
        saveFrameAsPhoto(outputPath, photoPath)
        // 删除临时视频文件
        val tempVideoFile = File(outputPath)
        if (tempVideoFile.exists()) {
            tempVideoFile.delete()
            try {
                MediaScannerConnection.scanFile(
                    eoRootUIViewModel.activity,
                    arrayOf(tempVideoFile.absolutePath),
                    null
                ) { _, _ ->
                    LogKit.d(TAG, "media scanner scan completed: ${tempVideoFile.absolutePath}")
                }
            } catch (e: NullPointerException) {
                LogKit.e(TAG, "media scanner scan failed: ${tempVideoFile.absolutePath}", e)
            }
            LogKit.i(TAG, "temp video file delete succeed: ${tempVideoFile.absolutePath}")
        }
    }

    override fun onError(error: Int, msg: String?) {
        LogKit.d(TAG, "onCompileError() called with: error = $error, msg = $msg")
        eoRootUIViewModel.exportActionState.postValue(ExportActionState.FAIlED(error, msg))
    }

    override fun onProgress(progress: Float) {
        LogKit.d(TAG, "onCompileProgress() : progress = ${progress.times(100).roundToInt()}")
        eoRootUIViewModel.exportActionState.postValue(
            ExportActionState.LOADING(
                progress.times(100).roundToInt()
            )
        )
    }

    private fun saveFrameAsPhoto(tempPath: String?, photoPath: File) {
        if (tempPath == null) return
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(tempPath)
            val frameBitmap = retriever.getFrameAtTime(0)
            val outStream = FileOutputStream(photoPath)
            frameBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
            MediaScannerConnection.scanFile(
                eoRootUIViewModel.activity,
                arrayOf(photoPath.absolutePath),
                null
            ) { _, _ ->
                // 导出图片成功
                LogKit.d(TAG, "onCompileDone() outputPath  $photoPath")
                if (finishAfterExport) {
                    ActivityResultExportListener(eoRootUIViewModel).onDone(photoPath.absolutePath)
                } else {
                    eoRootUIViewModel.exportActionState.postValue(ExportActionState.SUCCESS)
                }
            }
            outStream.close()

        } catch (e: Exception) {
            // 导出图片失败
            LogKit.e(TAG, "Get first frame from video failed.", e)
            eoRootUIViewModel.exportActionState.postValue(ExportActionState.FAIlED(-1, ""))
        } finally {
            retriever.release()
        }
    }

}