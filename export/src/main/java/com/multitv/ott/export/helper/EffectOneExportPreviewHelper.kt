package com.volcengine.effectone.export.helper

import android.graphics.Bitmap
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import com.multitv.ott.export.R
import com.volcengine.effectone.EffectOneSdk
import com.volcengine.effectone.editorui.external.data.EOVideoSize
import com.volcengine.effectone.export.base.IBaseUIHelper
import com.volcengine.effectone.export.data.isVisible
import com.volcengine.effectone.export.viewmode.EffectOneExportRootUIViewModel
import com.volcengine.effectone.export.viewmode.EffectOneExportViewModel
import com.volcengine.effectone.image.ImageOption

/**
 * 预览控制UI
 */
class EffectOneExportPreviewHelper(
    override val activity: FragmentActivity,
    override val owner: LifecycleOwner,
    private val root: View,
) : IBaseUIHelper {

    private val eoRootUIViewModel by lazy { EffectOneExportRootUIViewModel.get(activity) }
    private val eoExportViewModel by lazy { EffectOneExportViewModel.get(activity) }

    private val coverPreviewRoot by lazy { root.findViewById<FrameLayout>(R.id.export_cover_preview_root) }
    private val coverPreview by lazy { root.findViewById<SurfaceView>(R.id.export_cover_frame_preview) }
    private val coverImgPreview by lazy { root.findViewById<ImageView>(R.id.export_cover_img_preview) }
    override fun initView(rootViewGroup: ViewGroup) {
        //获取预览区大小，写入previewSize，后面获取封图大小需要使用到
        coverPreviewRoot?.let {
            it.viewTreeObserver?.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    it.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    eoRootUIViewModel.previewSize =
                        EOVideoSize(it.width, it.height)
                }
            })
        }
        initObserver()
    }

    private fun initObserver() {
        //控制SurfaceView或者ImageView显隐
        eoRootUIViewModel.showSurfacePreview.observe(owner) {
            coverPreview.isVisible = it
            coverImgPreview.isVisible = !it
        }

        //更新ImageView显示相册内容，发生在用户从相册选择封图
        eoExportViewModel.coverFromAlbum.observe(owner) {
            updateCoverImagePreView(it)
        }

        //更新ImageView显示视频帧内容，发生在用户保存视频帧成功
        eoExportViewModel.coverFromFrame.observe(owner) {
            updateCoverImagePreView(it?.first)
        }

        //控制ImageView显示视频帧还是相册封图，发生在退出封图编辑页面，回到默认UI
        eoRootUIViewModel.showFrameCover.observe(owner) {
            updateCoverImagePreView(if (it) eoExportViewModel.coverFromFrame.value?.first else eoExportViewModel.coverFromAlbum.value)
        }
    }

    private fun updateCoverImagePreView(it: String?) {
        it.takeIf { it.isNullOrEmpty().not() }?.let {
            EffectOneSdk.imageLoader.loadImageView(coverImgPreview, it, ImageOption.Builder().apply {
                width = coverImgPreview.width
                height = coverImgPreview.height
                scaleType = ImageView.ScaleType.CENTER
                format = Bitmap.Config.ARGB_8888
            }.build())
        }
    }

}
