package com.multitv.ott.export

import android.view.SurfaceView
import android.view.ViewGroup
import com.volcengine.ck.logkit.LogKit
import com.volcengine.effectone.export.base.EffectOneBaseFragment
import com.volcengine.effectone.export.data.isVisible
import com.volcengine.effectone.export.helper.EffectOneExportBottomBarHelper
import com.volcengine.effectone.export.helper.EffectOneExportPreviewHelper
import com.volcengine.effectone.export.helper.EffectOneExportTopBarHelper
import com.volcengine.effectone.export.viewmode.EffectOneExportRootUIViewModel
import com.volcengine.effectone.export.viewmode.EffectOneExportViewModel
import com.volcengine.effectone.widget.EOLoadingView

/**
 * 导出页根Fragment
 */
class EffectOneExportFragment : EffectOneBaseFragment() {
    companion object {
        const val TAG = "EffectOneExportFragment"
    }

    private val eoExportViewModel by lazy { EffectOneExportViewModel.get(requireActivity()) }
    private val eoRootUIViewModel by lazy { EffectOneExportRootUIViewModel.get(requireActivity()) }

    private val viewHelpers by lazy {
        mutableListOf(
            //顶部返回UI管理类
            EffectOneExportTopBarHelper(requireActivity(), viewLifecycleOwner, rootView),
            //预览UI管理类
            EffectOneExportPreviewHelper(requireActivity(), viewLifecycleOwner, rootView),
            //底部UI管理类
            EffectOneExportBottomBarHelper(requireActivity(), viewLifecycleOwner, rootView),
        )
    }
    private val coverPreview by lazy { rootView.findViewById<SurfaceView>(R.id.export_cover_frame_preview) }
    private val exportPageLoading by lazy { rootView.findViewById<EOLoadingView>(R.id.export_save_frame_loading) }
    override val layoutResourceId: Int
        get() = R.layout.eo_main_fragment_export

    override fun getFragmentTag() = tag ?: TAG

    override fun initView() {
        //>>>> step1 初始化EoEditorManager
        eoExportViewModel.initManager(requireActivity(), coverPreview) { suc ->
            if (!suc) {
                //初始化失败
                requireActivity().finish()
            } else {
                //剪辑页是否做过修改
                LogKit.i(
                    TAG,
                    "hasBeenEdited : ${eoExportViewModel.eoExportManager.eoExporter.eoExportConfig.hasBeenEdited}"
                )
                //剪辑视频的导入素材路径
                LogKit.i(
                    TAG,
                    "AddedMediasPath : ${eoExportViewModel.eoExportManager.eoExporter.getCurrentAddedMediasPath()}"
                )
                //剪辑页数据是否从草稿恢复进入
                LogKit.i(
                    TAG,
                    "isFromDraft : ${eoExportViewModel.eoExportManager.eoExporter.eoExportConfig.isFromDraft}"
                )
            }
        }
        //ui管理类控件初始化
        viewHelpers.forEach {
            it.initView(rootView as ViewGroup)
        }
    }

    override fun initObserver() {
        //注册生命周期observer
        viewHelpers.forEach {
            viewLifecycleOwner.lifecycle.addObserver(it)
        }

        //显示保存视频帧 loading view
        eoRootUIViewModel.showLoadingView.observe(viewLifecycleOwner) {
            exportPageLoading?.isVisible = it
        }

    }
}
