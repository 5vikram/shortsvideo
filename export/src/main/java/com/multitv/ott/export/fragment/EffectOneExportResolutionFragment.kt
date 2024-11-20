package com.volcengine.effectone.export.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import com.multitv.ott.export.R
import com.volcengine.effectone.api.EOVisibilityListener
import com.volcengine.effectone.editorui.external.data.EOFps
import com.volcengine.effectone.editorui.external.data.EOResolution
import com.volcengine.effectone.export.viewmode.EffectOneExportViewModel
import com.volcengine.effectone.export.viewmode.EffectOneVideoEncodeViewModel
import com.volcengine.effectone.ui.BaseBottomSheetDialogFragment

/**
 * 导出分辨率和FPS设置页面
 */
class EffectOneExportResolutionFragment : BaseBottomSheetDialogFragment() {
    companion object {
        const val TAG = "EffectOneExportResolutionFragment"
        fun show(activity: FragmentActivity) {
            EffectOneExportResolutionFragment().apply {
                setVisibilityListener(object : EOVisibilityListener {
                    override fun onDialogShow() {
                        //ignore
                    }

                    override fun onDialogDismiss() {
                        //ignore
                    }
                })
            }.show(activity.supportFragmentManager, TAG)
        }
    }

    private val effectOneVideoEncodeViewModel by lazy {
        EffectOneVideoEncodeViewModel.get(
            requireActivity()
        )
    }

    private val effectOneExportViewModel by lazy {
        EffectOneExportViewModel.get(
            requireActivity()
        )
    }

    private val viewOnClickListener by lazy {
        View.OnClickListener { v: View ->
            val parent_layout =
                v.parent as ConstraintLayout
            when (parent_layout.id) {
                R.id.resolution_list, R.id.fps_list -> {
                    //恢复按钮状态
                    val tvCount = parent_layout.childCount
                    for (i in 0 until tvCount) {
                        val curView = parent_layout.getChildAt(i)
                        curView.isSelected = false
                    }
                }
            }
            //点击分辨率
            when (v.id) {
                R.id.resolution_540p -> {
                    v.isSelected = true
                    effectOneVideoEncodeViewModel.changeSelectResolution(EOResolution.RES_540P)
                }

                R.id.resolution_720p -> {
                    v.isSelected = true
                    effectOneVideoEncodeViewModel.changeSelectResolution(EOResolution.RES_720P)
                }

                R.id.resolution_1080p -> {
                    v.isSelected = true
                    effectOneVideoEncodeViewModel.changeSelectResolution(EOResolution.RES_1080P)
                }

                R.id.resolution_4k -> {
                    v.isSelected = true
                    effectOneVideoEncodeViewModel.changeSelectResolution(EOResolution.RES_4K)
                }

                R.id.fps_25 -> {
                    v.isSelected = true
                    effectOneVideoEncodeViewModel.changeSelectFps(EOFps.FPS_25)
                }

                R.id.fps_30 -> {
                    v.isSelected = true
                    effectOneVideoEncodeViewModel.changeSelectFps(EOFps.FPS_30)

                }

                R.id.fps_50 -> {
                    v.isSelected = true
                    effectOneVideoEncodeViewModel.changeSelectFps(EOFps.FPS_50)
                }

                R.id.fps_60 -> {
                    v.isSelected = true
                    effectOneVideoEncodeViewModel.changeSelectFps(EOFps.FPS_60)
                }
                //点击重置
                R.id.resolution_fps_reset -> {
                    effectOneVideoEncodeViewModel.resetResolutionFps()
                    dismiss()
                }
                //点击保存
                R.id.resolution_fps_save -> {
                    effectOneVideoEncodeViewModel.saveResolutionFps()
                    dismiss()
                }
            }
        }
    }

    private lateinit var rootView: View
    override fun getFragmentTag() = tag ?: TAG

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.eo_main_fragment_export_resolution, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rootView = view
        initView(view)
        initData()
        hideFrameRateSetting(view)
    }

    //初始话默认选择item
    private fun initData() {
        val resolution = effectOneVideoEncodeViewModel.resolutionFps
            .value?.first
        val resolutionId: Int = when (resolution) {
            EOResolution.RES_540P -> R.id.resolution_540p
            EOResolution.RES_720P -> R.id.resolution_720p
            EOResolution.RES_4K -> R.id.resolution_4k
            EOResolution.RES_1080P -> R.id.resolution_1080p
            else -> R.id.resolution_720p
        }
        rootView.findViewById<TextView>(resolutionId).apply {
            isSelected = true
            resolution?.let { text = resolution.resValue }
        }

        val fps = effectOneVideoEncodeViewModel.resolutionFps
            .value?.second
        val fpsId: Int = when (fps) {
            EOFps.FPS_25 -> R.id.fps_25
            EOFps.FPS_30 -> R.id.fps_30
            EOFps.FPS_50 -> R.id.fps_50
            EOFps.FPS_60 -> R.id.fps_60
            else -> R.id.fps_30
        }
        rootView.findViewById<TextView>(fpsId).apply {
            isSelected = true
            fps?.let { text = it.fpsValue }
        }
    }

    //注册监听
    private fun initView(view: View) {
        view.findViewById<View>(R.id.resolution_540p).setOnClickListener(viewOnClickListener)
        view.findViewById<View>(R.id.resolution_720p).setOnClickListener(viewOnClickListener)
        view.findViewById<View>(R.id.resolution_1080p).setOnClickListener(viewOnClickListener)
        view.findViewById<View>(R.id.resolution_4k).setOnClickListener(viewOnClickListener)
        view.findViewById<View>(R.id.fps_25).setOnClickListener(viewOnClickListener)
        view.findViewById<View>(R.id.fps_30).setOnClickListener(viewOnClickListener)
        view.findViewById<View>(R.id.fps_50).setOnClickListener(viewOnClickListener)
        view.findViewById<View>(R.id.fps_60).setOnClickListener(viewOnClickListener)

        view.findViewById<View>(R.id.resolution_fps_reset).setOnClickListener(viewOnClickListener)
        view.findViewById<View>(R.id.resolution_fps_save).setOnClickListener(viewOnClickListener)
    }

    //隐藏帧率设置
    private fun hideFrameRateSetting(view: View) {
        // 判断是否在图片编辑模式中
        if (!effectOneExportViewModel.eoExportManager.checkPhotoEditingMode()) return
        view.findViewById<TextView>(R.id.fps_title).visibility = View.GONE
        view.findViewById<ConstraintLayout>(R.id.fps_list).visibility = View.GONE
    }
}