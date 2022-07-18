package com.upscapesoft.videodownloaderapp.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.upscapesoft.videodownloaderapp.BuildConfig
import com.upscapesoft.videodownloaderapp.R
import com.upscapesoft.videodownloaderapp.helper.AdController

class AboutActivity : AppCompatActivity() {

    private var appVersion: TextView? = null
    private var backBtn: ImageView? = null
    private var container: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        initViews()
        contentViews()
    }

    private fun initViews() {
        appVersion = findViewById(R.id.appVersion)
        backBtn = findViewById(R.id.backBtn)
        container = findViewById(R.id.banner_container)
    }

    @SuppressLint("SetTextI18n")
    private fun contentViews() {

        /*admob*/
        AdController.loadBannerAd(this@AboutActivity, container!!)
        AdController.loadInterAd(this@AboutActivity)

        appVersion!!.text = "Version " + BuildConfig.VERSION_NAME

        backBtn!!.setOnClickListener {
            onBackPressed()
        }

    }

    override fun onBackPressed() {
        AdController.adCounter++
        if (AdController.adCounter == AdController.adDisplayCounter) {
            AdController.showInterAd(this@AboutActivity, null, 0)
        } else {
            super.onBackPressed()
        }
    }

}