package com.example.finapay.ui.register

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Patterns
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
import com.example.finapay.utils.FormUtils
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegisterActivity : AppCompatActivity() {
    private val viewModel: RegisterViewModel by viewModels()
    private lateinit var formUtils: FormUtils

    private lateinit var nameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText

    private lateinit var nameInputLayout: TextInputLayout
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var confirmPasswordInputLayout: TextInputLayout

    private lateinit var registerButton: Button
    private lateinit var registerProgress: ProgressBar

    private lateinit var bgWave2: ImageView
    private lateinit var bgWave3: ImageView
    private lateinit var animation: Animation

    private lateinit var loginPrompt: TextView

    private var loginButtonIsEnabled: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initViews()
        setupListener()
        observeViewModel()
    }

    private fun initViews() {
        formUtils = FormUtils()
        nameInputLayout = findViewById(R.id.etNameLayout)
        emailInputLayout = findViewById(R.id.etEmailLayout)
        passwordInputLayout = findViewById(R.id.etPasswordLayout)
        confirmPasswordInputLayout = findViewById(R.id.etConfirmPasswordLayout)

        nameInput = findViewById(R.id.etName)
        emailInput = findViewById(R.id.etEmail)
        passwordInput = findViewById(R.id.etPassword)
        confirmPasswordInput = findViewById(R.id.etConfirmPassword)

        registerButton = findViewById(R.id.btnRegister)
        registerProgress = findViewById(R.id.btnProgressBar)
        registerButton.backgroundTintList = null
        loginPrompt = findViewById(R.id.tvLoginPrompt)

        bgWave2 = findViewById(R.id.bgWave2)
        bgWave3 = findViewById(R.id.bgWave3)
        animation = Animation()
        animation.animationslidebottom(bgWave2, 550)
        animation.animationslidebottom(bgWave3)

        setLoginPrompt()
        formUtils.clearErrorOnInput(nameInputLayout, nameInput)
        formUtils.clearErrorOnInput(emailInputLayout, emailInput)
        formUtils.clearErrorOnInput(passwordInputLayout, passwordInput)
        formUtils.clearErrorOnInput(confirmPasswordInputLayout, confirmPasswordInput)
    }

    private fun disableView() {
        registerButton.isEnabled = false
        nameInput.isEnabled = false
        emailInput.isEnabled = false
        passwordInput.isEnabled = false
        confirmPasswordInput.isEnabled = false
        registerButton.isEnabled = false
        registerButton.text = ""
        loginButtonIsEnabled = false
    }

    private fun enableView() {
        registerButton.isEnabled = true
        nameInput.isEnabled = true
        emailInput.isEnabled = true
        passwordInput.isEnabled = true
        confirmPasswordInput.isEnabled = true
        registerButton.isEnabled = true
        registerButton.text = "Register"
        loginButtonIsEnabled = true
    }

    private fun setLoginPrompt() {
        val fullText = "Already have an account? Login"
        val spannable = SpannableString(fullText)

        val start = fullText.indexOf("Login")
        val end = start + "Login".length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                if (!loginButtonIsEnabled) return
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

        loginPrompt.text = spannable
        loginPrompt.movementMethod = LinkMovementMethod.getInstance()
        loginPrompt.highlightColor = Color.TRANSPARENT

    }

    private fun setupListener() {
        registerButton.setOnClickListener {
            if (!validateForm()) return@setOnClickListener
            disableView()
            registerProgress.visibility = View.VISIBLE

            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            val name = nameInput.text.toString()

            viewModel.register(email, password, name, this)
        }
    }

    private fun validateForm(): Boolean {
        window.decorView.clearFocus()

        var isValid = true
        var firstInvalidView: View? = null

        fun validateEditText(
            layout: TextInputLayout,
            input: TextInputEditText,
            fieldName: String,
            isEmail: Boolean = false
        ) {
            val text = input.text?.toString()?.trim()
            if (text.isNullOrEmpty()) {
                layout.error = "$fieldName wajib diisi"
                layout.boxStrokeErrorColor =
                    ContextCompat.getColorStateList(this, R.color.error_red)
                if (firstInvalidView == null) firstInvalidView = input
                isValid = false
            } else if (isEmail && !Patterns.EMAIL_ADDRESS.matcher(text).matches()) {
                layout.error = "Format $fieldName tidak valid"
                layout.boxStrokeErrorColor =
                    ContextCompat.getColorStateList(this, R.color.error_red)
                if (firstInvalidView == null) firstInvalidView = input
                isValid = false
            } else {
                layout.error = null
            }
        }

        // Validasi masing-masing input
        validateEditText(nameInputLayout, nameInput, "Nama")
        validateEditText(emailInputLayout, emailInput, "Email", isEmail = true)
        validateEditText(passwordInputLayout, passwordInput, "Password")
        validateEditText(confirmPasswordInputLayout, confirmPasswordInput, "Konfirmasi password")

        // Validasi kesesuaian password dan konfirmasi password
        val password = passwordInput.text?.toString()?.trim()
        val confirmPassword = confirmPasswordInput.text?.toString()?.trim()
        if (!password.isNullOrEmpty() && !confirmPassword.isNullOrEmpty() && password != confirmPassword) {
            confirmPasswordInputLayout.error = "Konfirmasi password tidak sesuai"
            confirmPasswordInputLayout.boxStrokeErrorColor =
                ContextCompat.getColorStateList(this, R.color.error_red)
            if (firstInvalidView == null) firstInvalidView = confirmPasswordInput
            isValid = false
        }

        firstInvalidView?.requestFocus()

        return isValid
    }

    private fun observeViewModel() {
        viewModel.registerSuccess.observe(this) { user ->
            enableView()
            registerProgress.visibility = View.GONE

            user?.let {
                CustomDialog.show(
                    context = this,
                    iconRes = R.drawable.ic_outline_check_circle_24,
                    title = "Registrasi Berhasil!",
                    message = "Akun kamu sudah terdaftar. Silakan cek email untuk otentikasi.",
                    primaryButtonText = "Kembali Login",
                    primaryButtonBackgroundRes = R.drawable.color_button_blue,
                    iconColor = R.color.blue,
                    onPrimaryClick = {
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                )

            }
        }


        viewModel.registerError.observe(this) { errorMessage ->
            enableView()
            registerProgress.visibility = View.GONE

            errorMessage?.let {
                CustomDialog.show(
                    context = this,
                    iconRes = R.drawable.ic_outline_cancel_presentation_24,
                    title = "Registrasi Gagal!",
                    message = errorMessage,
                    primaryButtonText = "OK",
                    primaryButtonBackgroundRes = R.drawable.color_button_red,
                    iconColor = R.color.red
                )
            }

        }
    }
}