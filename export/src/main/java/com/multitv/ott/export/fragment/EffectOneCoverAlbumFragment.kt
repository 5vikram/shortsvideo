package com.volcengine.effectone.export.fragment

import android.view.View
import com.multitv.ott.export.R
import com.volcengine.effectone.export.base.EffectOneBaseFragment
import com.volcengine.effectone.export.viewmode.EffectOneExportViewModel

/**
 * 从相册选择封图->重选
 */
class EffectOneCoverAlbumFragment :
    EffectOneBaseFragment() {
    companion object {
        const val TAG = "EffectOneCoverAlbumFragment"
        fun newInstance(): EffectOneCoverAlbumFragment {
            return EffectOneCoverAlbumFragment()
        }
    }

    private val eoExportViewModel by lazy { EffectOneExportViewModel.get(requireActivity()) }
    override val layoutResourceId: Int
        get() = R.layout.eo_main_fragment_export_cover_album


    override fun getFragmentTag() = tag ?: TAG

    override fun initView() {
        rootView.findViewById<View>(R.id.export_cover_album_reselect)?.setOnClickListener {
            //拉起相册
            eoExportViewModel.pickAlbum()
        }
    }
}
