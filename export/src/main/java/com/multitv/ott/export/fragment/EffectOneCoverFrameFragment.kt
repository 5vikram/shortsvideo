package com.volcengine.effectone.export.fragment

import com.bytedance.ies.nle.editor_jni.VecLongLong
import com.multitv.ott.export.R
import com.volcengine.effectone.editorui.external.data.EOVideoSize
import com.volcengine.effectone.editorui.widget.EOEditorFrameListView
import com.volcengine.effectone.export.base.EffectOneBaseFragment
import com.volcengine.effectone.export.viewmode.EffectOneExportViewModel
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * 从视频帧选择封图
 */
class EffectOneCoverFrameFragment :
    EffectOneBaseFragment() {
    companion object {
        const val TAG = "EffectOneCoverFrameFragment"
        fun newInstance(): EffectOneCoverFrameFragment {
            return EffectOneCoverFrameFragment()
        }
    }

    private val eoExportViewModel by lazy {
        EffectOneExportViewModel.get(requireActivity())
    }

    private val frameListView by lazy { rootView.findViewById<EOEditorFrameListView>(R.id.export_cover_frame) }
    override val layoutResourceId: Int
        get() = R.layout.eo_main_fragment_export_cover_frame

    override fun getFragmentTag() = tag ?: TAG

    override fun initView() {
        frameListView?.apply {
            //滑动监听
            onProgressChanged = { progress, final ->
                eoExportViewModel.eoExportManager.getTotalVideoDuration()?.let {
                    val realTime: Long =
                        (progress * 1F / EOEditorFrameListView.MAX_PROGRESS * it).roundToLong()
                    //根据用户seek，跳转视频位置 final是视频最后
                    eoExportViewModel.seekTo(realTime, final.not())
                }
            }
        }
    }

    override fun initData() {
        frameListView?.apply {
            post {
                //获取多个视频帧，填充adapter
                initFrameList()
            }
        }
    }

    private fun initFrameList() {
        frameListView ?: return
        val itemCount = EOEditorFrameListView.ITEM_COUNT
        val totalDuration = eoExportViewModel.eoExportManager.getTotalVideoDuration() ?: return
        val gap = totalDuration / itemCount
        val timeStamps = mutableListOf<Long>()
        val start = gap / 2

        repeat(itemCount) { index ->
            timeStamps.add((start + gap * index))
        }
        eoExportViewModel.eoExportManager.getVideoFrames(
            VecLongLong(timeStamps),
            EOVideoSize(
                width = frameListView.getItemWidth(),
                height = (frameListView.getItemWidth() * 16F / 9F).roundToInt()
            )
        ) { videoFrame ->
            videoFrame?.let {
                frameListView.addFrameData(it)
                trySeekToFramePosition()
            }
        }
    }

    /**
     * 滑动上次抽帧的位置
     */
    private fun trySeekToFramePosition() {
        frameListView ?: return
        val exportViewModel = eoExportViewModel.eoExportManager.getEOExportModel() ?: return
        val totalDuration = eoExportViewModel.eoExportManager.getTotalVideoDuration() ?: return
        eoExportViewModel.coverFromFrame.takeIf { exportViewModel.isAlbumCover.not() }
            ?.let { coverFramePair ->
                coverFramePair.value?.let {
                    eoExportViewModel.seekTo(it.second, false)
                    val progress = it.second.times(1f).div(totalDuration)
                        .times(EOEditorFrameListView.MAX_PROGRESS).roundToInt()
                    frameListView?.setProgress(progress)
                }
            }
    }
}
