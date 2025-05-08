package com.example.finapay.ui.splash

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.finapay.MainActivity
import com.example.finapay.R
import com.example.finapay.ui.landing.LandingActivity
import com.example.finapay.ui.login.LoginActivity
import com.example.finapay.utils.SharedPreferencesHelper

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_FINAPay)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)


        val logoImage: ImageView = findViewById(R.id.logoImage)
        val anim = AnimationUtils.loadAnimation(this, R.anim.splash_fade_in)
        logoImage.startAnimation(anim)

        val sharedPref = SharedPreferencesHelper(this)
        val user = sharedPref.getUserData()

        Handler(Looper.getMainLooper()).postDelayed({
            val nextActivity = if (user?.token != null) {
                Intent(this, MainActivity::class.java)
            } else{
                Intent(this, LandingActivity::class.java)
            }
//            val nextActivity = Intent(this, LandingActivity::class.java)


            nextActivity.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(nextActivity)
            finish()
        }, 2000)
    }



}