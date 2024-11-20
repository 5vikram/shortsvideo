package com.volcengine.effectone.export.viewmode

import android.graphics.Bitmap
import android.view.SurfaceView
import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.multitv.ott.export.EffectOneExportActivity
import com.volcengine.ck.album.AlbumEntrance
import com.volcengine.effectone.editorui.external.EOExportManager
import com.volcengine.effectone.editorui.external.data.EOVideoSize
import com.volcengine.effectone.permission.Scene
import com.volcengine.effectone.utils.EOUtils
import com.volcengine.effectone.viewmodel.BaseViewModel
import com.volcengine.effectone.viewmodel.EffectOneViewModelFactory

/**
 * 导出页Manager 管理类
 * 记录视频帧
 * 相册封面等
 */
class EffectOneExportViewModel(activity: FragmentActivity) : BaseViewModel(activity) {
    companion object {
        const val TAG = "EffectOneExportViewModel"
        fun get(activity: FragmentActivity): EffectOneExportViewModel {
            return EffectOneViewModelFactory.viewModelProvider(activity)[EffectOneExportViewModel::class.java]
        }
    }

    //记录从相册选择的封面
    val coverFromAlbum = MutableLiveData<String>()

    //记录保存的视频帧
    val coverFromFrame = MutableLiveData<Pair<String, Long>>()

    //记录相册选择成功，切换tab
    val coverSelectSuc = MutableLiveData<Unit>()

    //导出管理Manager
    lateinit var eoExportManager: EOExportManager

    //eoExportManager初始化成功
    val eoExportManagerInitSuc = MutableLiveData<Boolean>()

    //更是视频帧封图
    fun updateCoverFrame(videoFrame: String, position: Long) {
        coverFromFrame.value = videoFrame to position
    }

    //更新相册封图
    private fun updateCoverAlbum(videoFrame: String) {
        coverFromAlbum.value = videoFrame
    }

    //是否选择过相册封图
    fun hasCoverAlbum(): Boolean {
        return coverFromAlbum.let { it.value?.isEmpty() == false }
    }

    //是否保存过视频帧封图
    private fun hasCoverFrame(): Boolean {
        return coverFromFrame.let { it.value?.first?.isEmpty() == false }
    }

    //是否有封图
    fun hasCoverImageSelected(): Boolean {
        return hasCoverAlbum() || hasCoverFrame()
    }

    //重置记录
    fun resetSavedCover() {
        coverFromFrame.value = null
        coverFromAlbum.value = null
        initDefaultCover()
    }

    //初始化Manager
    fun initManager(
        activity: ComponentActivity,
        surfaceView: SurfaceView? = null,
        previewSurfaceImage: Bitmap? = null,
        callback: (suc: Boolean) -> Unit

    ) {
        val callbackWrapper  = {suc:Boolean ->
            eoExportManagerInitSuc.postValue(suc)
            callback.invoke(suc)
            if(suc){
                initDefaultCover()
                eoExportManager.shouldAddWaterMark(true)
                eoExportManager.setWaterMarkPath(EffectOneExportActivity.waterMarkPath)
                eoExportManager.setWaterMarkSize(EOVideoSize(29,31))
            }
        }
        eoExportManager = EOExportManager.init(activity, surfaceView, previewSurfaceImage, callback = callbackWrapper)
    }

    //恢复进入页面默认的封图
    private fun initDefaultCover() {
        eoExportManager.getVideoCoverImage { draftImg, framePosition, isAlbumCover ->
            if (isAlbumCover) {
                updateCoverAlbum(draftImg)
            } else {
                updateCoverFrame(draftImg, framePosition)
            }
        }
    }

    //设置视频位置
    fun seekTo(position: Long, isSmooth: Boolean) {
        eoExportManager.seekTo(position, isSmooth)
    }

    //保存视频帧
    fun saveVideoFrame(
        videoSize: EOVideoSize,
        saveToDraft: Boolean = true,
        savedBlock: ((String?, Long) -> Unit)? = null
    ) {
        eoExportManager.saveVideoFrame(videoSize, saveToDraft, savedBlock)
    }

    //打开相册
    fun pickAlbum() {
        activity?.let { act ->
            EOUtils.permission.checkPermissions(act, Scene.ALBUM ,{
                eoExportManager.pickAlbum { bitmapPath: String?, errorMsg: String?, cancel: Boolean ->
                    bitmapPath?.let {
                        updateCoverAlbum(it)
                        coverSelectSuc.value = Unit
                    }
                }
            }, {
                AlbumEntrance.showAlbumPermissionTips(act)
            })
        }
    }
}
