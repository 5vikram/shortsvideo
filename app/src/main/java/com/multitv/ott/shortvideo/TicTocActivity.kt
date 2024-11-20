/*
package com.multitv.ott.shortvideo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.multitv.ott.shortvideo.databinding.ShortVideoLayoutBinding
import com.multitv.ott.shortvideo.utils.CacheUttils

class TicTocActivity : AppCompatActivity() {
    private lateinit var binding: ShortVideoLayoutBinding
    private val contentHomeList = ArrayList<ContentItem>()

    private var endPointContentListUrl =
        "https://expo.multitvsolution.com/api/v6/content/list/token/66fa6b4ca3961/device/android/current_offset/0/max_counter/100/cat_id/5371"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.short_video_layout)

        val homeData = CacheUttils.getHomeCacheData()
        if (homeData != null && homeData.result != null && homeData.result.content != null && homeData.result.content.size > 0) {
            binding.loadMoreProgressbar.visibility = View.GONE
            binding.centerProgressbar.visibility = View.GONE
            contentHomeList.addAll(homeData.result.content)
            if (contentHomeList.size != 0) {
                binding.rvVideo.visibility = View.VISIBLE
                binding.contentInfoNotFoundTV.visibility = View.GONE
            } else {
                binding.rvVideo.visibility = View.GONE
                binding.contentInfoNotFoundTV.visibility = View.VISIBLE
                //getVideoDetailsData(false)
            }
        } else {
            //getVideoDetailsData(false)
        }
    }
}*/
