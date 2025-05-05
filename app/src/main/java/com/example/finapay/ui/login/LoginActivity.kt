package com.example.finapay.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.finapay.MainActivity
import com.example.finapay.R

class LoginActivity : AppCompatActivity(){
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_FINAPay)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailField = findViewById<EditText>(R.id.etEmail)
        val passwordField = findViewById<EditText>(R.id.etPassword)
        val loginButton = findViewById<Button>(R.id.btnLogin)
        val loginProgress = findViewById<ProgressBar>(R.id.btnProgressBar)

        loginButton.backgroundTintList=null

        val textView = findViewById<TextView>(R.id.tvRegisterPrompt)
        val fullText = "Don't have an account? Register"
        val spannable = SpannableString(fullText)

        val start = fullText.indexOf("Register")
        val end = start + "Register".length

        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.bright_blue)),
            start, end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        textView.text = spannable

        loginButton.setOnClickListener {
            loginProgress.visibility = View.VISIBLE
            loginButton.text = "" // Hilangkan teks "Login"
            loginButton.isEnabled = false

            val email = emailField.text.toString()
            val password = passwordField.text.toString()
            viewModel.login(email, password, this)
        }

        viewModel.loginSuccess.observe(this) { user ->
            loginProgress.visibility = View.GONE
            loginButton.text = "Login"
            loginButton.isEnabled = true

            user?.let {
                Toast.makeText(this, "Selamat datang, ${it.name}", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

        viewModel.loginError.observe(this) { errorMessage ->
            loginProgress.visibility = View.GONE
            loginButton.text = "Login"
            loginButton.isEnabled = true

            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }

    }
}