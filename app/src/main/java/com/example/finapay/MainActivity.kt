package com.example.finapay

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.finapay.ui.login.LoginActivity
import com.example.finapay.utils.SharedPreferencesHelper

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val sharedPreferencesHelper = SharedPreferencesHelper(this)
        val user = sharedPreferencesHelper.getUserData()

        val welcomeText = findViewById<TextView>(R.id.tvWelcome)
        if (user != null) {
            welcomeText.text = "Selamat datang, ${user.name}"
        } else {
            welcomeText.text = "Gagal mengambil data pengguna."
        }


        val logoutButton = findViewById<Button>(R.id.btnLogout)
        logoutButton.setOnClickListener {
            // Menghapus data user dari SharedPreferences
            sharedPreferencesHelper.clearUserData()

            // Mengarahkan kembali ke LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}