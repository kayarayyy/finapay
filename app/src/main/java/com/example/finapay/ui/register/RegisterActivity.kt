package com.example.finapay.ui.register

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.finapay.R
import com.example.finapay.ui.animation.Animation
import com.example.finapay.ui.login.LoginActivity
import com.example.finapay.utils.CustomDialog

class RegisterActivity : AppCompatActivity() {
    private val viewModel: RegisterViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_FINAPay)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val emailField = findViewById<EditText>(R.id.etEmail)
        val passwordField = findViewById<EditText>(R.id.etPassword)
        val nameField = findViewById<EditText>(R.id.etName)
        val confirmPasswordField = findViewById<EditText>(R.id.etConfirmPassword)
        val registerButton = findViewById<Button>(R.id.btnRegister)
        val registerProgress = findViewById<ProgressBar>(R.id.btnProgressBar)
        var bgWave2 = findViewById<ImageView>(R.id.bgWave2)
        var bgWave3 = findViewById<ImageView>(R.id.bgWave3)
        val animation = Animation()
        animation.animationslideleft(bgWave2)
        animation.animationslideleft(bgWave3, 550)

        registerButton.backgroundTintList=null


        val textView = findViewById<TextView>(R.id.tvLoginPrompt)
        val fullText = "Already have an account? Login"
        val spannable = SpannableString(fullText)

        val start = fullText.indexOf("Login")
        val end = start + "Login".length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false // Hilangkan underline
                ds.color = ContextCompat.getColor(this@RegisterActivity, R.color.blue) // Warna biru
            }
        }

        spannable.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        textView.text = spannable
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT


        registerButton.setOnClickListener{
            registerProgress.visibility = View.VISIBLE
            registerButton.text = "" // Hilangkan teks "Login"
            registerButton.isEnabled = false

            val email = emailField.text.toString()
            val password = passwordField.text.toString()
            val name = nameField.text.toString()
            val confirmPassword = confirmPasswordField.text.toString()

            viewModel.register(email, password, name, this)
        }

        viewModel.registerSuccess.observe(this) { user ->
            registerProgress.visibility = View.GONE
            registerButton.text = "Register"
            registerButton.isEnabled = true

            user?.let {
                CustomDialog.show(
                    context = this,
                    iconRes = R.drawable.ic_baseline_add_task_24,
                    title = "Registrasi Berhasil!",
                    message = "Akun kamu sudah terdaftar. Silakan cek email untuk otentikasi.",
                    buttonText = "Kembali Login",
                    buttonBackgroundRes = R.drawable.color_button_blue,
                    iconColor = R.color.blue
                ) {
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }

            }
        }


        viewModel.registerError.observe(this) { errorMessage ->
            registerProgress.visibility = View.GONE
            registerButton.text = "Register"
            registerButton.isEnabled = true

            errorMessage?.let {
                CustomDialog.show(
                    context = this,
                    iconRes = R.drawable.ic_baseline_360_24,
                    title = "Registrasi Gagal!",
                    message = errorMessage,
                    buttonText = "OK",
                    buttonBackgroundRes = R.drawable.color_button_red,
                    iconColor = R.color.red
                )
            }

        }
    }


}