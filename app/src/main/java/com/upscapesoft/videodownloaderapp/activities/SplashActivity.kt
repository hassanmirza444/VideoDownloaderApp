package com.upscapesoft.videodownloaderapp.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.upscapesoft.videodownloaderapp.R
import com.upscapesoft.videodownloaderapp.data.Constants
import java.util.*

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    var activity: SplashActivity? = null

    private var appNameSplash: TextView? = null
    private var companyName: TextView? = null
    private var appLoader: LottieAnimationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        activity = this

        initViews()
        contentViews()
        mainScreen()

    }

    private fun initViews() {
        appNameSplash = findViewById(R.id.appNameSplash)
        companyName = findViewById(R.id.companyName)
        appLoader = findViewById(R.id.appLoader)
    }

    private fun contentViews() {
        appNameSplash!!.alpha = 0f
        appNameSplash!!.animate()
            .translationY(appNameSplash!!.height.toFloat())
            .alpha(1f)
            .setDuration(1000)
            .setStartDelay(1000)
            .translationY(appNameSplash!!.height.toFloat())
            .alpha(1f)
            .setDuration(1200).startDelay = 1500

        companyName!!.alpha = 0f
        companyName!!.animate()
            .translationY(companyName!!.height.toFloat())
            .alpha(1f)
            .setDuration(1000)
            .setStartDelay(1000)
            .translationY(companyName!!.height.toFloat())
            .alpha(1f)
            .setDuration(1200).startDelay = 1500

        appLoader!!.alpha = 0f
        appLoader!!.animate()
            .translationY(appLoader!!.height.toFloat())
            .alpha(1f)
            .setDuration(1000)
            .setStartDelay(1000)
            .translationY(appLoader!!.height.toFloat())
            .alpha(1f)
            .setDuration(1200).startDelay = 1500
    }

    @Suppress("deprecation")
    private fun mainScreen() {
        Handler().postDelayed({
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }, Constants.SPLASH_SCREEN_TIMEOUT.toLong())
    }

}