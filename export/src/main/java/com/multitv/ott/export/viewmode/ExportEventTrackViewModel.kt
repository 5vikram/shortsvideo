package com.volcengine.effectone.export.viewmode

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.ss.android.vesdk.VECommonCallbackInfo
import com.volcengine.effectone.viewmodel.BaseViewModel
import com.volcengine.effectone.viewmodel.EffectOneViewModelFactory.Companion.viewModelProvider

@Suppress("UnusedPrivateMember", "EmptyFunctionBlock", "FunctionOnlyReturningConstant")
class ExportEventTrackViewModel(activity: FragmentActivity) : BaseViewModel(activity) {

    private val eoExportViewModel by lazy { EffectOneExportViewModel.get(activity) }
    private val eoRootUIViewModel by lazy { EffectOneExportRootUIViewModel.get(activity) }

    companion object {
        const val TAG = "ExportEventTrackViewModel"
        fun get(activity: FragmentActivity): ExportEventTrackViewModel {
            return viewModelProvider(activity).get(ExportEventTrackViewModel::class.java)
        }
    }

    fun initExportObserver(viewLifecycleOwner: LifecycleOwner) {

        eoRootUIViewModel.exportActionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ExportActionState.FAIlED -> {
                    updateExportEndTime()
                    buildReportExportLogParam(
                        mutableMapOf<String, String>().apply {
                            put("errorCode", "error = ${state.error}, msg = ${state.msg}")
                            put("status", "failed")
                        })
                }
                is ExportActionState.SUCCESS -> {
                    updateExportEndTime()
                    buildReportExportLogParam(
                        eoRootUIViewModel.getReportParam()?.apply {
                            put("status", "success")
                        })
                    exportLogDoReport()
                }

                else -> {}
            }
        }

        eoExportViewModel.eoExportManagerInitSuc.observe(viewLifecycleOwner){
            if (it) {
                addOnInfoListener()
            }
        }
    }

    fun exportVideoEvent(savePath: String, hardCode: Int) {
        updateExportStartTime()
        updateExportPath(savePath)
        updateExportVideoHardCodec(hardCode)
    }

    fun exportImageEvent(savePath: String, hardCode: Int) {
        updateExportStartTime()
        updateExportPath(savePath)
        updateExportVideoHardCodec(hardCode)
    }

    private fun buildReportExportLogParam(extra: MutableMap<String, String>? = null) {
        if (!canBuildReportParam()) return
        updateReportExportParam(buildReportParam(extra))
    }

    private fun addOnInfoListener() {
        if (!canBuildReportParam()) return
        eoExportViewModel.eoExportManager.addInfoLisenter { type, _, _, _ ->
            if (VECommonCallbackInfo.TE_INFO_FIRST_FRAME == type) {
                updateExportFirstFrameTime()
                publishLogDoReport()
            }
        }
    }

    private fun buildReportParam(extra: MutableMap<String, String>?): MutableMap<String, String>? {
        val reportParam: MutableMap<String, String> = mutableMapOf()
        extra?.let {
            reportParam.putAll(it)
        }
        return reportParam
    }

    private fun canBuildReportParam(): Boolean {
        return false
    }

    private fun updateReportExportParam(param: MutableMap<String, String>?) {

    }

    private fun exportLogDoReport() {

    }

    private fun publishLogDoReport() {

    }

    private fun updateExportEndTime() {

    }

    private fun updateExportStartTime() {

    }

    private fun updateExportPath(exportPath: String) {

    }

    private fun updateExportVideoHardCodec(hardCode: Int) {

    }

    private fun updateExportFirstFrameTime() {

    }
}
