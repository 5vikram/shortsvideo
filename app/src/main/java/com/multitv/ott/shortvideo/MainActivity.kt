package com.multitv.ott.shortvideo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.multitv.ott.shortvideo.appcontroller.ApplicationController
import com.multitv.ott.shortvideo.databinding.ActivityMainBinding
import com.multitv.ott.shortvideo.network.CommonApiListener
import com.multitv.ott.shortvideo.network.CommonApiPresenterImpl
import com.multitv.ott.shortvideo.network.Json
import com.multitv.ott.shortvideo.service.VideoPreLoadingService
import com.multitv.ott.shortvideo.uttls.PlayerConstant

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var videoList = arrayListOf<String>()
    private var endPointContentListUrl =
        "https://expo.multitvsolution.com/api/v6/content/list/token/66fa6b4ca3961/device/android/current_offset/0/max_counter/100/cat_id/5371"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        getVideoDetailsData()

    }

    private fun startPreLoadingService() {
        val preloadingServiceIntent = Intent(this, VideoPreLoadingService::class.java)
        preloadingServiceIntent.putStringArrayListExtra(PlayerConstant.VIDEO_LIST, videoList)
        startService(preloadingServiceIntent)

        Handler(Looper.getMainLooper()).postDelayed({
            binding.centerProgressbar.visibility = View.GONE
            val intent = Intent(this, ShortsVideoActivity::class.java)
            startActivity(intent)
            finish()
        }, 1000)
    }

    private val token =
        "abd07061a3dd9851e3c9dd551e68e26838b29e87b2baa479c0eb53c95cac2e6b9835d5a1b6b51c5dfb0febddf90338d925096a08fe746a6bd615205b5d1a29545d1b3c9fb826b9e787a6e8307c77423d"


    private fun getVideoDetailsData() {
        binding.centerProgressbar.visibility = View.VISIBLE

        val header = HashMap<String, String>()
        header.put("authorization", token)

        CommonApiPresenterImpl(object : CommonApiListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onSuccess(response: String?) {
                val home = Json.parse(response, ShortVideo::class.java) as ShortVideo
                ApplicationController.getInstance().refershHomeData()
                ApplicationController.getInstance().getCacheManager()
                    ?.put("Home", home)


                for (items in home.result?.content!!) {
                    videoList.add(items.url.toString())
                }

                if (videoList.isNotEmpty()) {
                    startPreLoadingService()
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.centerProgressbar.visibility = View.GONE
                        val intent = Intent(this@MainActivity, ShortsVideoActivity::class.java)
                        startActivity(intent)
                        finish()
                    }, 1000)
                }
            }

            override fun onError(message: String?) {
                binding.centerProgressbar.visibility = View.VISIBLE
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.centerProgressbar.visibility = View.GONE
                    val intent = Intent(this@MainActivity, ShortsVideoActivity::class.java)
                    startActivity(intent)
                    finish()
                }, 1000)
            }

        }).getRequest(endPointContentListUrl, "Content List Url", header)

    }

}