package com.volcengine.effectone.export.helper

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.multitv.ott.export.R
import com.volcengine.effectone.export.base.IBaseUIHelper
import com.volcengine.effectone.export.data.PageScenes
import com.volcengine.effectone.export.data.isVisible
import com.volcengine.effectone.export.viewmode.ActionEvent
import com.volcengine.effectone.export.viewmode.EffectOneExportRootUIViewModel
import com.volcengine.effectone.extensions.setNoDoubleClickListener

/**
 * 顶部控制栏UI
 * 1.默认返回按钮 -> click -> DEFAULT_BACK
 * 2.图片编辑的 “取消” ->click ->COVER_EDITOR_CANCEL
 *            “确认”按钮 ->click ->COVER_EDITOR_DONE
 * 3.导出 “取消”按钮 ->click ->EXPORT_CANCEL
 */
class EffectOneExportTopBarHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner,
    private val root: View,
) : IBaseUIHelper {

    private val eoRootUIViewModel by lazy { EffectOneExportRootUIViewModel.get(activity) }

    private val exportDefaultBack by lazy { root.findViewById<View>(R.id.export_default_back) }

    private val exportCoverEditorContainer by lazy { root.findViewById<View>(R.id.export_cover_editor_container) }
    private val exportCoverEditorCancel by lazy { root.findViewById<View>(R.id.export_cover_editor_cancel) }
    private val exportCoverEditorDone by lazy { root.findViewById<View>(R.id.export_cover_editor_done) }

    private val exportActionCancel by lazy { root.findViewById<View>(R.id.export_action_cancel) }

    override fun initView(rootViewGroup: ViewGroup) {
        //默认页面（PageScenes.DEFAULT)，左上角返回按钮发生点击
        exportDefaultBack?.setNoDoubleClickListener {
            eoRootUIViewModel.actionEvent.value = ActionEvent.DEFAULT_BACK
        }

        //封图编辑（PageScenes.COVER_EDITOR)，左上角取消按钮发生点击
        exportCoverEditorCancel?.setNoDoubleClickListener {
            eoRootUIViewModel.actionEvent.value = ActionEvent.COVER_EDITOR_CANCEL
        }

        //封图编辑（PageScenes.COVER_EDITOR)，右上角确认按钮发生点击
        exportCoverEditorDone?.setNoDoubleClickListener {
            eoRootUIViewModel.actionEvent.value = ActionEvent.COVER_EDITOR_DONE
        }

        //导出页面（PageScenes.EXPORT)，左上角取消按钮发生点击
        exportActionCancel?.setOnClickListener {
            eoRootUIViewModel.actionEvent.value = ActionEvent.EXPORT_CANCEL
        }

        initObserver()

    }

    private fun initObserver() {
        eoRootUIViewModel.pageSense.observe(owner) { pageScenes ->
            when (pageScenes) {
                PageScenes.DEFAULT  -> {
                    //默认页面显示exportDefaultBack，其他隐藏
                    exportDefaultBack.isVisible = true
                    exportCoverEditorContainer.isVisible = false
                    exportActionCancel.isVisible = false
                }

                PageScenes.COVER_EDITOR -> {
                    exportDefaultBack.isVisible = false
                    //预览编辑页面显示exportCoverEditorContainer，其他隐藏
                    exportCoverEditorContainer.isVisible = true
                    exportActionCancel.isVisible = false
                }

                PageScenes.EXPORT -> {
                    exportDefaultBack.isVisible = false
                    exportCoverEditorContainer.isVisible = false
                    //导出页面显示exportActionCancel，其他隐藏
                    exportActionCancel.isVisible = true
                }
                else->{}
            }
        }
    }
}