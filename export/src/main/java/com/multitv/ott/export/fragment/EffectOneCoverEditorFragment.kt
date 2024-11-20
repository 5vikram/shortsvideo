package com.volcengine.effectone.export.fragment

import android.view.MotionEvent
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.multitv.ott.export.R
import com.volcengine.effectone.export.base.EffectOneBaseFragment
import com.volcengine.effectone.export.data.PageScenes
import com.volcengine.effectone.export.data.TabType
import com.volcengine.effectone.export.data.isVisible
import com.volcengine.effectone.export.viewmode.ActionEvent
import com.volcengine.effectone.export.viewmode.EffectOneExportRootUIViewModel
import com.volcengine.effectone.export.viewmode.EffectOneExportViewModel

/**
 * 封图编辑入口页
 * ViewPager2+TabLayout
 * @see TabType 视频帧和相册 两个tab
 * @see PageScenes.COVER_EDITOR
 *
 */
class EffectOneCoverEditorFragment :
    EffectOneBaseFragment() {
    companion object {
        const val TAG = "EffectOneCoverEditorFragment"
    }

    private val eoRootUIViewModel by lazy { EffectOneExportRootUIViewModel.get(requireActivity()) }
    private val eoExportViewModel by lazy { EffectOneExportViewModel.get(requireActivity()) }

    private val viewPager2 by lazy { rootView.findViewById<ViewPager2>(R.id.eo_cover_editor_viewpager) }
    private val tabLayout by lazy { rootView.findViewById<TabLayout>(R.id.eo_cover_editor_tablayout) }

    private val innerFragmentAdapter by lazy {
        InnerFragmentAdapter(
            this,
            TabType.values().toList()
        )
    }

    private lateinit var tabLayoutMediator: TabLayoutMediator
    private lateinit var currentSelectTabType: TabType
    override val layoutResourceId: Int
        get() = R.layout.eo_main_fragment_export_cover_editor

    override fun getFragmentTag() = tag ?: TAG
    override fun initView() {
        viewPager2.apply {
            adapter = innerFragmentAdapter
            //禁用手势滑动切换tab
            isUserInputEnabled = false
        }

        tabLayoutMediator =
            TabLayoutMediator(tabLayout, viewPager2, true, false) { tab, pos ->
                tab.text = innerFragmentAdapter.getTabName(pos)
            }

        tabLayout?.apply {
            //注册tab点击监听
            addOnTabSelectedListener(onTabSelectedListener())
        }

        tabLayoutMediator.attach()

        tabLayout?.apply {
            //从相册选择图片优先判断是否已经选择过了相册图片 hasCoverAlbum ,没有的话未拦截事件，不切换tab，拉起相册页面
            val albumTab = tabLayout.getTabAt(1)
            albumTab?.let {
                val tabParent = it.view as? ViewGroup
                tabParent?.let {
                    tabParent.setOnTouchListener { _, event ->
                        if (eoExportViewModel.hasCoverAlbum()) {
                            return@setOnTouchListener false
                        } else {
                            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                                eoExportViewModel.pickAlbum()
                            }
                            return@setOnTouchListener true
                        }
                    }
                }
            }
        }
    }


    override fun initObserver() {
        //PageScenes  ui页面切换监听
        eoRootUIViewModel.pageSense.observe(viewLifecycleOwner) { pageScenes ->
           when (pageScenes) {
                PageScenes.DEFAULT -> {
                    rootView.isVisible = false
                }

                PageScenes.COVER_EDITOR -> {
                    //显示封图编辑，其他scenes隐藏
                    rootView.isVisible = true
                    //每次进来默认选择第一个tab
                    tabLayout?.takeIf {
                        it.tabCount>0
                    }?.apply {
                        //先重置一下
                       selectTab(null)
                        selectTab(getTabAt(0),true)
                    }
                }

                PageScenes.EXPORT -> {
                    rootView.isVisible = false
                }
               else->{}
            }
        }

        //顶部取消和确认按钮点击监听
        eoRootUIViewModel.actionEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                //用户点击左上角取消，回退默认ui（DEFAULT），并显示SurfaceView
                ActionEvent.COVER_EDITOR_CANCEL -> {
                    eoRootUIViewModel.showSurfacePreview.value = true
                    eoRootUIViewModel.changeScenes(PageScenes.DEFAULT)
                    //清除记录的内容
                    eoExportViewModel.resetSavedCover()
                }

                //用户点击右上角确认，需要根据当前tab，做响应处理
                ActionEvent.COVER_EDITOR_DONE -> {
                    //视频帧tab，保存视频帧，回退默认ui（DEFAULT），显示SurfaceView需要根据是否有做保存过封图
                    if (isFrameTabSelect()) {
                        //显示loading
                        eoRootUIViewModel.showLoadingView.value = true
                        //保存视频帧
                        eoExportViewModel.saveVideoFrame(
                            eoRootUIViewModel.previewSize,
                        ) { bitmapPath, position ->
                            eoRootUIViewModel.showLoadingView.value = false
                            bitmapPath?.let {
                                //更新视频帧封图到 coverFromFrame
                                eoExportViewModel.updateCoverFrame(it, position)
                            }
                            backToDefaultScenes()
                        }
                    } else {
                        //回退默认ui（DEFAULT），显示SurfaceView需要根据是否有做保存过封图
                        backToDefaultScenes()
                    }
                }

                else -> {}
            }
        }

        //监听相册选择图监听，成功的话，尝试切换tab
        eoExportViewModel.coverSelectSuc.observe(viewLifecycleOwner) {

            val albumPos = innerFragmentAdapter.getTabPos(TabType.ALBUM)
            val curPos = innerFragmentAdapter.getTabPos(currentSelectTabType)
            viewPager2?.takeIf { albumPos != curPos }?.setCurrentItem(albumPos, false)
        }
    }

    private fun backToDefaultScenes() {
        eoRootUIViewModel.changeScenes(PageScenes.DEFAULT)
        //判断从哪个tab回到default UI，使用视频帧还是相册封图
        eoRootUIViewModel.showFrameCover.value = isFrameTabSelect()
        eoRootUIViewModel.showSurfacePreview.value =
            eoExportViewModel.hasCoverImageSelected().not()
    }

    private fun isFrameTabSelect() = currentSelectTabType == TabType.FRAME

    private fun onTabSelectedListener() = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            //记录当前选中的tab
            currentSelectTabType = innerFragmentAdapter.getTabType(tab.position)
            //切换tab控制预览显示SurfaceView还是ImageView
            eoRootUIViewModel.showSurfacePreview.value = isFrameTabSelect()
            //控制预览ImageView显示内容来源
            eoRootUIViewModel.showFrameCover.value = isFrameTabSelect()
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {
            //ignore
        }

        override fun onTabReselected(tab: TabLayout.Tab) {
            //ignore
        }
    }
}

internal class InnerFragmentAdapter(
    private val fragment: Fragment,
    private val tabList: List<TabType>,
) : FragmentStateAdapter(fragment) {

    private val fragmentMap = mutableMapOf<TabType, Fragment>()
    override fun getItemCount() = tabList.size

    override fun createFragment(position: Int): Fragment {
        val tab = tabList[position]
        return getFragmentByType(tab)
    }

    fun getTabName(pos: Int): CharSequence {
        return when (tabList[pos]) {
            TabType.ALBUM -> fragment.getString(R.string.eo_export_cover_album)
            TabType.FRAME -> fragment.getString(R.string.eo_export_cover_frame)
        }
    }

    //根据tab找到pos，找不到就返回默认0
    fun getTabPos(tab: TabType): Int {
        return tabList.indexOfFirst {
            tab == it
        }.takeIf { it >= 0 } ?: 0
    }

    fun getTabType(position: Int): TabType {
        return tabList.takeIf { position in 0 until itemCount }?.let { tabList[position] }
            ?: TabType.FRAME
    }

    private fun getFragmentByType(tab: TabType): Fragment {
        val fragment = fragmentMap[tab]
        if (fragment != null) {
            return fragment
        }
        return when (tab) {
            TabType.FRAME -> {
                EffectOneCoverFrameFragment.newInstance()
            }

            TabType.ALBUM -> {
                EffectOneCoverAlbumFragment.newInstance()
            }
        }.apply {
            fragmentMap[tab] = this
        }
    }
}
