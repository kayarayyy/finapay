package com.example.finapay.ui.change_passowrd

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.finapay.MainActivity
import com.example.finapay.R
import com.example.finapay.ui.animation.Animation
import com.example.finapay.utils.CustomDialog
import com.example.finapay.utils.FormUtils
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ChangePasswordActivity : AppCompatActivity() {
    private val viewModel: ChangePasswordViewModel by viewModels()
    private lateinit var formUtils: FormUtils

    private lateinit var oldPasswordInput: TextInputEditText
    private lateinit var newPasswordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText

    private lateinit var oldPasswordInputLayout: TextInputLayout
    private lateinit var newPasswordInputLayout: TextInputLayout
    private lateinit var confirmPasswordInputLayout: TextInputLayout

    private lateinit var changePasswordButton: Button
    private lateinit var backButton: ImageView
    private lateinit var changePasswordProgress: ProgressBar

    private lateinit var bgWave2: ImageView
    private lateinit var bgWave3: ImageView
    private lateinit var animation: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        initViews()
        setupListener()
        observeViewModel()
    }

    private fun initViews() {
        formUtils = FormUtils()
        oldPasswordInputLayout = findViewById(R.id.etOldPasswordLayout)
        newPasswordInputLayout = findViewById(R.id.etNewPasswordLayout)
        confirmPasswordInputLayout = findViewById(R.id.etConfirmPasswordLayout)

        oldPasswordInput = findViewById(R.id.etOldPassword)
        newPasswordInput = findViewById(R.id.etNewPassword)
        confirmPasswordInput = findViewById(R.id.etConfirmPassword)

        changePasswordButton = findViewById(R.id.btnChangePassword)
        changePasswordProgress = findViewById(R.id.btnProgressBar)
        backButton = findViewById(R.id.btnBack)
        changePasswordButton.backgroundTintList = null

        bgWave2 = findViewById(R.id.bgWave2)
        bgWave3 = findViewById(R.id.bgWave3)
        animation = Animation()
        animation.animationslidebottom(bgWave2, 550)
        animation.animationslidebottom(bgWave3)

        formUtils.clearErrorOnInput(oldPasswordInputLayout, oldPasswordInput)
        formUtils.clearErrorOnInput(newPasswordInputLayout, newPasswordInput)
        formUtils.clearErrorOnInput(confirmPasswordInputLayout, confirmPasswordInput)
    }

    private fun disableView() {
        oldPasswordInput.isEnabled = false
        newPasswordInput.isEnabled = false
        confirmPasswordInput.isEnabled = false
        changePasswordButton.isEnabled = false
        changePasswordButton.text = ""
    }

    private fun enableView() {
        oldPasswordInput.isEnabled = true
        newPasswordInput.isEnabled = true
        confirmPasswordInput.isEnabled = true
        changePasswordButton.isEnabled = true
        changePasswordButton.text = "Submit"
    }

    private fun setupListener() {
        changePasswordButton.setOnClickListener {
            submitData()
        }
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun submitData() {
        if (!validateForm()) return

        val oldPassword = oldPasswordInput.text.toString()
        val newPassword = newPasswordInput.text.toString()
        val confirmPassword = confirmPasswordInput.text.toString()

        CustomDialog.show(
            context = this,
            iconRes = R.drawable.ic_baseline_assignment_add_24,
            title = "Ubah Password?",
            message = "Pastikan password anda sudah terisi dengan benar",
            primaryButtonText = "Ya",
            primaryButtonBackgroundRes = R.drawable.color_button_blue,
            secondaryButtonText = "Batal",
            secondaryButtonBackgroundRes = R.drawable.color_button_gray,
            onPrimaryClick = {
                disableView()
                viewModel.changePassword(oldPassword, newPassword, confirmPassword)
            },
            iconColor = R.color.blue,
        )

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
        validateEditText(oldPasswordInputLayout, oldPasswordInput, "Password Lama")
        validateEditText(newPasswordInputLayout, newPasswordInput, "Password Baru")
        validateEditText(confirmPasswordInputLayout, confirmPasswordInput, "Konfirmasi password")

        // Validasi kesesuaian password dan konfirmasi password
        val password = newPasswordInput.text?.toString()?.trim()
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
        viewModel.changePasswordSuccess.observe(this) { success ->
            CustomDialog.show(
                context = this,
                iconRes = R.drawable.ic_outline_check_circle_24,
                title = "Sukses!",
                message = "Password anda berhasil diubah",
                primaryButtonText = "OK",
                primaryButtonBackgroundRes = R.drawable.color_button_blue,
                onPrimaryClick = {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("destination", "profile")
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                },
                iconColor = R.color.blue
            )
        }
        viewModel.changePasswordError.observe(this) { error ->
            CustomDialog.show(
                context = this,
                iconRes = R.drawable.ic_outline_dangerous_24,
                title = "Gagal!",
                message = error,
                primaryButtonText = "OK",
                primaryButtonBackgroundRes = R.drawable.color_button_blue,
                iconColor = R.color.red
            )
            enableView()
        }
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                disableView()
                changePasswordProgress.visibility = View.VISIBLE
            } else {
                enableView()
                changePasswordProgress.visibility = View.GONE
            }
        }
    }
}