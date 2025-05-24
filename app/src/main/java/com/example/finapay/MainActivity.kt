package com.example.finapay

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.finapay.data.sources.ApiClient
import com.example.finapay.ui.history.HistoryFragment
import com.example.finapay.ui.home.HomeFragment
import com.example.finapay.ui.profile.ProfileFragment
import com.example.finapay.utils.SharedPreferencesHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val token = SharedPreferencesHelper(this).getUserData()?.token
        ApiClient.init { token }

        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Baca intent extra
        val destination = intent.getStringExtra("destination")

        when (destination) {
            "profile" -> {
                replaceFragment(ProfileFragment())
                bottomNav.selectedItemId = R.id.nav_profile
            }
            "history" -> {
                replaceFragment(HistoryFragment())
                bottomNav.selectedItemId = R.id.nav_history
            }
            else -> {
                replaceFragment(HomeFragment())
                bottomNav.selectedItemId = R.id.nav_home
            }
        }

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.nav_history -> {
                    replaceFragment(HistoryFragment())
                    true
                }
                R.id.nav_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }


    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }


}
