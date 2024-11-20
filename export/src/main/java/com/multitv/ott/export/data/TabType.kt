package com.volcengine.effectone.export.data

import android.view.View
import com.volcengine.effectone.export.data.PageScenes.COVER_EDITOR
import com.volcengine.effectone.export.data.PageScenes.DEFAULT
import com.volcengine.effectone.export.data.PageScenes.EXPORT
import com.volcengine.effectone.export.data.TabType.ALBUM
import com.volcengine.effectone.export.data.TabType.FRAME

/**
 * 封图编辑Tab
 * @see FRAME :视频帧tab
 * @see ALBUM :相册tab
 */
enum class TabType {
    FRAME,
    ALBUM,
}

/**
 * 页面类型Scenes，控制ui显示和交互逻辑
 * @see DEFAULT :导出页默认UI （返回按钮，封图编辑，导出，分辨率设置）
 * @see COVER_EDITOR：封图设置页面：ViewPager2+TabLayout,工两个tab，视频帧和相册
 * @see EXPORT 导出页： 导出进度loading，取消导出
 *
 */
enum class PageScenes {
    DEFAULT,
    COVER_EDITOR,
    EXPORT,
}


var View.isVisible: Boolean
    get() = this.visibility == View.VISIBLE
    set(value) {
        takeUnless { value == isVisible }?.let {
            this.visibility = if (value) View.VISIBLE else View.GONE
        }
    }