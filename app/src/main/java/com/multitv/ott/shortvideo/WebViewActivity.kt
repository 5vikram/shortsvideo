package com.multitv.ott.shortvideo

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WebViewActivity : AppCompatActivity() {

    private var url: String = ""

    private lateinit var imageButton: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var webView: WebView
    private lateinit var boldTextView3: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        imageButton = findViewById(R.id.imageButton)
        progressBar = findViewById(R.id.progressBar)
        webView = findViewById(R.id.webView)
        boldTextView3 = findViewById(R.id.boldTextView3)
        url = intent.getStringExtra("url").toString()
        title = intent.getStringExtra("title").toString()

        if (!TextUtils.isEmpty(title))
            boldTextView3.setText(title)

        imageButton.setOnClickListener {
            finish()
        }

        loadWebView(url)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadWebView(url: String) {
        progressBar.visibility = View.VISIBLE
        try {
            webView.loadUrl(url)
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            //webView.settings.javaScriptCanOpenWindowsAutomatically = true
            webView.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
            //webView.settings.lightTouchEnabled = false
            // webView.isHorizontalScrollBarEnabled = false

            webView.webViewClient = object : WebViewClient() {

                override fun onPageFinished(webView: WebView?, url: String) {
                    super.onPageFinished(webView, url)
                    progressBar.visibility = View.GONE
                }

                @Deprecated("Deprecated in Java")
                override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
                    progressBar.visibility = View.VISIBLE
                    webView.loadUrl(url)
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


}