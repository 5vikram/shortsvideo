package com.volcengine.effectone.export.fragment

import com.multitv.ott.export.R
import com.ss.ugc.android.editor.track.utils.visible
import com.volcengine.effectone.export.base.EffectOneBaseFragment
import com.volcengine.effectone.export.data.PageScenes
import com.volcengine.effectone.export.viewmode.ActionEvent
import com.volcengine.effectone.export.viewmode.EffectOneExportRootUIViewModel
import com.volcengine.effectone.export.viewmode.ExportActionState
import com.volcengine.effectone.export.widget.EffectOneProgressSeekBar

/**
 * 导出进度ui
 * @see PageScenes.EXPORT
 * @see ExportActionState 导出状态相关的事件
 */
class EffectOneExportProgressFragment :
    EffectOneBaseFragment() {
    companion object {
        const val TAG = "EffectOneExportActionFragment"
    }

    private val eoRootUIViewModel by lazy { EffectOneExportRootUIViewModel.get(requireActivity()) }

    private var exportProgressbar: EffectOneProgressSeekBar? = null
    override val layoutResourceId: Int
        get() = R.layout.eo_main_layout_export_progress

    override fun getFragmentTag() = tag ?: TAG
    override fun initView() {
        exportProgressbar = rootView.findViewById(R.id.export_loading_seekbar)
        exportProgressbar?.apply {
            isEnabled = false
            setRange(0, 100)
            setSuffixIndicator("%")
            //修改ui和预览view宽度相同
            layoutParams.width = eoRootUIViewModel.previewSize.width
        }
    }

    override fun initObserver() {
        //导出相关监听
        eoRootUIViewModel.exportActionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ExportActionState.LOADING -> {
                    exportProgressbar?.progress = state.progress
                }

                else -> {}
            }
        }

        //用户取消导出点击事件监听
        eoRootUIViewModel.actionEvent.observe(viewLifecycleOwner) { state ->
            when (state) {

                ActionEvent.EXPORT_CANCEL -> {
                    rootView.visible = true
                }

                else -> {}
            }
        }

        //PageScenes ui控制
        eoRootUIViewModel.pageSense.observe(viewLifecycleOwner) { pageScenes ->
            when (pageScenes) {
                PageScenes.EXPORT -> {
                    rootView.visible = true
                }

                else -> {
                    //重置进度
                    exportProgressbar?.progress = 0
                    rootView.visible = false
                }
            }
        }
    }
}