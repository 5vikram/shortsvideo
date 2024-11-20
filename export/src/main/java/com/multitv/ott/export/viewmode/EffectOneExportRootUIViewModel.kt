package com.volcengine.effectone.export.viewmode

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.volcengine.effectone.editorui.external.data.EOVideoSize
import com.volcengine.effectone.export.data.PageScenes
import com.volcengine.effectone.utils.SizeUtil
import com.volcengine.effectone.viewmodel.BaseViewModel
import com.volcengine.effectone.viewmodel.EffectOneViewModelFactory

/**
 * 导出状态
 */
sealed class ExportActionState {
    /**
     * 导出成功
     */
    object SUCCESS : ExportActionState()

    /**
     * 导出失败
     * @param error 错误码
     * @param msg 错误日志
     */
    data class FAIlED(val error: Int, var msg: String?) : ExportActionState()

    /**
     * @param progress 到出中。。。取值范围0~100
     */
    data class LOADING(val progress: Int) : ExportActionState()
}

/**
 * 用户点击事件
 */
enum class ActionEvent(val eventName: String) {
    DEFAULT_BACK("默认左上角返回"),
    COVER_EDITOR_CANCEL("封面编辑，取消"),
    COVER_EDITOR_DONE("封面编辑，确认"),
    EXPORT_CANCEL("导出，取消"),
    EXPORT_ACTION("默认，导出按钮"),
}

class EffectOneExportRootUIViewModel(activity: FragmentActivity) : BaseViewModel(activity) {


    companion object {
        fun get(activity: FragmentActivity): EffectOneExportRootUIViewModel {
            return EffectOneViewModelFactory.viewModelProvider(activity)[EffectOneExportRootUIViewModel::class.java]
        }
    }

    //记录当前page
    val pageSense = MutableLiveData<PageScenes>()

    //记录预览组件大小，用于保存视频帧
    var previewSize =
        EOVideoSize(SizeUtil.getScreenWidth(activity), SizeUtil.getScreenHeight(activity))

    //显示封图编辑页面
    val showCoverEditorView = MutableLiveData<Boolean>()

    //显示导出页面
    val showExportProgressView = MutableLiveData<Boolean>()

    //是否执行导出逻辑
    val actionEvent = MutableLiveData<ActionEvent>()

    //导出状态
    val exportActionState = MutableLiveData<ExportActionState>()

    //显示预览SurfaceView
    val showSurfacePreview = MutableLiveData<Boolean>()

    //展示Image图来源视频帧
    val showFrameCover = MutableLiveData<Boolean>()

    //显示保存视频帧loading view
    val showLoadingView = MutableLiveData<Boolean>()


    //切换Page
    fun changeScenes(scenes: PageScenes) {
        pageSense.takeIf { pageSense.value != scenes }?.let {
            pageSense.setValue(scenes)
        }
    }

}