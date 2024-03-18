package com.multitv.ott.shortvideo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.multitv.ott.shortvideo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.launchSdkButton.setOnClickListener {
            val intent = Intent(this, ShortsVideoActivity::class.java)
            intent.putExtra(
                "contentUrl",
                "https://expo.multitvsolution.com/api/v6/content/list/token/15zh353kd4dese/device/android/current_offset/0/max_counter/100/cat_id/3437"
            )
            startActivity(intent)
            finish()
        }
    }
}