package com.multitv.ott.shortvideo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.multitv.ott.shortvideo.adapter.WatchListNewAdapter
import com.multitv.ott.shortvideo.databinding.ActivityLoginBinding
import com.multitv.ott.shortvideo.fragment.EmailFreagment
import com.multitv.ott.shortvideo.fragment.MobileFragment

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private var watchList = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        watchList.add("Mobile")
        watchList.add("Email")

        binding.viewPagerWatchList.adapter =
            WatchListNewAdapter(supportFragmentManager, watchList)
        binding.tabLayoutWatchList.setupWithViewPager(binding.viewPagerWatchList)
        binding.viewPagerWatchList.offscreenPageLimit = 1

        binding.viewPagerWatchList.setOnPageChangeListener(object :
            ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> MobileFragment.newInstance()
                    1 -> EmailFreagment.newInstance()
                    else -> { // Note the block
                        MobileFragment.newInstance()
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }

        })
    }


}