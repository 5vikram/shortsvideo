package com.volcengine.effectone.export.helper

import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.multitv.ott.export.R
import com.ss.ugc.android.editor.track.utils.visible
import com.volcengine.effectone.export.base.IBaseUIHelper
import com.volcengine.effectone.export.data.PageScenes
import com.volcengine.effectone.export.viewmode.ActionEvent
import com.volcengine.effectone.export.viewmode.EffectOneExportRootUIViewModel
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 底部ui控制：
 *  1.管理封图stub inflate 和 跳转
 *  2.管理导出stub inflate 和 跳转
 */
class EffectOneExportBottomBarHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner,
    private val root: View,
) : IBaseUIHelper {

    private val eoRootUIViewModel by lazy { EffectOneExportRootUIViewModel.get(activity) }

    private val exportCoverEditorStub by lazy { root.findViewById<ViewStub>(R.id.export_cover_editor_stub) }
    private val hasCoverEditorStubInflate = AtomicBoolean(false)

    private val exportProgressStub by lazy { root.findViewById<ViewStub>(R.id.export_progress_stub) }
    private val hasExportProgressStubInflate = AtomicBoolean(false)
    override fun initView(rootViewGroup: ViewGroup) {
        //注册封图编辑ui stub inflate监听
        exportCoverEditorStub.setOnInflateListener { _, _ ->
            hasCoverEditorStubInflate.compareAndSet(false, true)
            eoRootUIViewModel.changeScenes(PageScenes.COVER_EDITOR)
        }
        //注册导出ui stub inflate监听
        exportProgressStub.setOnInflateListener { _, _ ->
            hasExportProgressStubInflate.compareAndSet(false, true)
            eoRootUIViewModel.actionEvent.value = ActionEvent.EXPORT_ACTION
        }
        initObserver()
    }

    private fun initObserver() {
        //封图编辑ui stub inflate 跳转 COVER_EDITOR
        eoRootUIViewModel.showCoverEditorView.observe(owner) {
            exportCoverEditorStub.takeUnless { hasCoverEditorStubInflate.get() }?.let {
                exportCoverEditorStub.visible = true
            } ?: run {
                eoRootUIViewModel.changeScenes(PageScenes.COVER_EDITOR)
            }
        }
        //导出ui stub inflate 跳转 COVER_EDITOR
        eoRootUIViewModel.showExportProgressView.observe(owner) {
            exportProgressStub.takeUnless { hasExportProgressStubInflate.get() }?.let {
                exportProgressStub.visible = true
            } ?: run {
                eoRootUIViewModel.actionEvent.value = ActionEvent.EXPORT_ACTION
            }
        }
    }
}