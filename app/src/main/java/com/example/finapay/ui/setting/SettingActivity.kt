package com.example.finapay.ui.setting

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.example.finapay.MainActivity
import com.example.finapay.R
import com.example.finapay.ui.change_password.ChangePasswordActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var switchDarkMode: SwitchCompat
    private lateinit var switchNotifications: SwitchCompat
    private lateinit var settingLanguage: LinearLayout
    private lateinit var settingChangePassword: LinearLayout
    private lateinit var settingPrivacy: LinearLayout
    private lateinit var settingAboutApp: LinearLayout
    private lateinit var settingHelp: LinearLayout
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
        }

        initViews()
        setupListeners()
        loadSettings()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        switchDarkMode = findViewById(R.id.switch_dark_mode)
        switchNotifications = findViewById(R.id.switch_notifications)
        settingLanguage = findViewById(R.id.setting_language)
        settingChangePassword = findViewById(R.id.setting_change_password)
        settingPrivacy = findViewById(R.id.setting_privacy)
        settingAboutApp = findViewById(R.id.setting_about_app)
        settingHelp = findViewById(R.id.setting_help)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("destination", "profile")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            saveSettings("dark_mode", isChecked)
        }

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            saveSettings("notifications", isChecked)
        }

        settingLanguage.setOnClickListener {
            showLanguageDialog()
        }

        settingChangePassword.setOnClickListener {
             val intent = Intent(this, ChangePasswordActivity::class.java)
             startActivity(intent)
        }

        settingPrivacy.setOnClickListener {
        }

        settingAboutApp.setOnClickListener {
        }

        settingHelp.setOnClickListener {
        }

    }

    private fun loadSettings() {
        // Muat pengaturan dari SharedPreferences
        val preferences = getSharedPreferences("app_settings", MODE_PRIVATE)
        val isDarkMode = preferences.getBoolean("dark_mode", false)
        val isNotificationsEnabled = preferences.getBoolean("notifications", true)

        // Atur switch sesuai nilai yang disimpan
        switchDarkMode.isChecked = isDarkMode
        switchNotifications.isChecked = isNotificationsEnabled
    }

    private fun saveSettings(key: String, value: Boolean) {
        // Simpan pengaturan ke SharedPreferences
        getSharedPreferences("app_settings", MODE_PRIVATE)
            .edit()
            .putBoolean(key, value)
            .apply()
    }

    private fun showLanguageDialog() {
        // Implementasi dialog untuk memilih bahasa aplikasi
        // Contoh implementasi:

        val languages = arrayOf("Indonesia", "English", "日本語")
        AlertDialog.Builder(this)
            .setTitle("Pilih Bahasa")
            .setItems(languages) { _, which ->
                // Simpan preferensi bahasa yang dipilih
                val tvLanguageValue = findViewById<TextView>(R.id.tv_language_value)
                tvLanguageValue.text = languages[which]

                // Simpan ke SharedPreferences dan ubah locale aplikasi
                saveLanguageSettings(which)
            }
            .show()
    }

    private fun saveLanguageSettings(languageIndex: Int) {
        // Simpan setting bahasa dan terapkan ke aplikasi
        val languageCode = when (languageIndex) {
            0 -> "id" // Indonesia
            1 -> "en" // English
            2 -> "ja" // Japanese
            else -> "id"
        }

        // Simpan kode bahasa ke SharedPreferences
        getSharedPreferences("app_settings", MODE_PRIVATE)
            .edit()
            .putString("language", languageCode)
            .apply()

        // Terapkan perubahan bahasa (implementasikan sesuai kebutuhan)
        // updateLocale(languageCode)
    }
}