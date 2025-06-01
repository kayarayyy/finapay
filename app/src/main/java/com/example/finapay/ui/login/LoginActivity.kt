package com.example.finapay.ui.login

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.finapay.MainActivity
import com.example.finapay.R
import com.example.finapay.data.models.requests.FcmRegisterRequest
import com.example.finapay.ui.animation.Animation
import com.example.finapay.ui.register.RegisterActivity
import com.example.finapay.utils.CustomDialog
import com.example.finapay.utils.FormUtils
import com.example.finapay.utils.SharedPreferencesHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var formUtils: FormUtils
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText

    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout

    private lateinit var loginButton: Button
    private lateinit var loginGoogleButton: SignInButton
    private lateinit var loginProgress: ProgressBar

    private lateinit var bgWave: ImageView
    private lateinit var bgWave1: ImageView
    private lateinit var animation: Animation

    private lateinit var registerPrompt: TextView
    private lateinit var tvForgotPassword: TextView

    private var registerButtonIsEnabled: Boolean = true

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
//        val loginProgress = findViewById<ProgressBar>(R.id.btnProgressBar)
        Log.d("GoogleLogin", "res code: ${result.resultCode} & ${result.data}")

        if (result.resultCode == RESULT_OK && result.data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val tokenId = account?.idToken

                Log.d("GoogleLogin", "Login TokenID: $tokenId")

                if (tokenId != null) {
                    getFCMToken { fcmToken ->
                        viewModel.signInGoogle(tokenId, fcmToken ?: "", this)
                    }
                } else {
                    enableView()
                    loginProgress.visibility = View.GONE
                    CustomDialog.show(
                        context = this,
                        iconRes = R.drawable.ic_outline_cancel_presentation_24,
                        title = "Login Gagal!",
                        message = "Terjadi kesalahan saat login. Gagal mendapatkan token.",
                        primaryButtonText = "OK",
                        primaryButtonBackgroundRes = R.drawable.color_button_red,
                        iconColor = R.color.red
                    )
                    Toast.makeText(this, "Token ID tidak tersedia", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                enableView()
                loginProgress.visibility = View.GONE
                CustomDialog.show(
                    context = this,
                    iconRes = R.drawable.ic_outline_cancel_presentation_24,
                    title = "Login Gagal!",
                    message = "Terjadi kesalahan saat login. Aplikasi tidak diizinkan.",
                    primaryButtonText = "OK",
                    primaryButtonBackgroundRes = R.drawable.color_button_red,
                    iconColor = R.color.red
                )
                Log.e("GoogleLogin", "Login gagal, code=${e.statusCode}", e)
                Toast.makeText(
                    this,
                    "Login Google gagal: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            enableView()
            loginProgress.visibility = View.GONE
            CustomDialog.show(
                context = this,
                iconRes = R.drawable.ic_outline_cancel_presentation_24,
                title = "Login Gagal!",
                message = "Terjadi kesalahan saat login. Sign in dibatalkan.",
                primaryButtonText = "OK",
                primaryButtonBackgroundRes = R.drawable.color_button_red,
                iconColor = R.color.red
            )

            Log.e("GoogleLogin", "Login dibatalkan oleh pengguna.")
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("Permission", "Notification permission granted")
        } else {
            Log.d("Permission", "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_login)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        askNotificationPermission()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("630482567435-4ck2eqarg55ua42v9g2d2gmj5kgphfv5.apps.googleusercontent.com") // Ganti dengan Client ID milikmu
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        initViews()
        setupListener()
        observeViewModel()
    }

    private fun initViews() {
        formUtils = FormUtils()
        sharedPreferencesHelper = SharedPreferencesHelper(this)
        emailInputLayout = findViewById(R.id.etEmailLayout)
        passwordInputLayout = findViewById(R.id.etPasswordLayout)
        emailInput = findViewById(R.id.etEmail)
        passwordInput = findViewById(R.id.etPassword)
        loginButton = findViewById(R.id.btnLogin)
        loginGoogleButton = findViewById(R.id.btn_google_sign_in)
        loginProgress = findViewById(R.id.btnProgressBar)
        bgWave = findViewById(R.id.bgWave)
        bgWave1 = findViewById(R.id.bgWave1)
        registerPrompt = findViewById(R.id.tvRegisterPrompt)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        animation = Animation()
        animation.animationslidebottom(bgWave)
        animation.animationslidebottom(bgWave1, 550)
        loginButton.backgroundTintList = null
        setRegisterPrompt()

        formUtils.clearErrorOnInput(emailInputLayout, emailInput)
        formUtils.clearErrorOnInput(passwordInputLayout, passwordInput)
    }

    private fun disableView() {
        loginGoogleButton.isEnabled = false
        emailInput.isEnabled = false
        passwordInput.isEnabled = false
        loginButton.text = ""
        loginButton.isEnabled = false
        registerButtonIsEnabled = false
    }

    private fun enableView() {
        loginGoogleButton.isEnabled = true
        emailInput.isEnabled = true
        passwordInput.isEnabled = true
        loginButton.text = "Login"
        loginButton.isEnabled = true
        registerButtonIsEnabled = true
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

        validateEditText(emailInputLayout, emailInput, "Email", isEmail = true)
        validateEditText(passwordInputLayout, passwordInput, "Password")

        firstInvalidView?.requestFocus()

        return isValid
    }

    private fun setupListener() {
        tvForgotPassword.setOnClickListener {
            showEmailInputDialog(this)
        }
        loginGoogleButton.setOnClickListener {
            sharedPreferencesHelper.clearUserData()
            disableView()
            loginProgress.visibility = View.VISIBLE

            // Sign out terlebih dahulu untuk memastikan clean session
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
        }

        loginButton.setOnClickListener {
            if (!validateForm()) return@setOnClickListener
            sharedPreferencesHelper.clearUserData()
            disableView()
            loginProgress.visibility = View.VISIBLE

            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            getFCMToken { token ->
                if (token != null) {
                    viewModel.login(email, password, token, this)
                } else {
                    viewModel.login(email, password, "", this)
                }
            }
        }
    }

    private fun setRegisterPrompt() {
        val fullText = "Don't have an account? Register"
        val spannable = SpannableString(fullText)

        val start = fullText.indexOf("Register")
        val end = start + "Register".length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                if (!registerButtonIsEnabled) return
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    overrideActivityTransition(
                        OVERRIDE_TRANSITION_OPEN,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                } else {
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false // Hilangkan underline
                ds.color = ContextCompat.getColor(this@LoginActivity, R.color.blue) // Warna biru
            }
        }

        spannable.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        registerPrompt.text = spannable
        registerPrompt.movementMethod = LinkMovementMethod.getInstance()
        registerPrompt.highlightColor = Color.TRANSPARENT
    }

    private fun observeViewModel() {
        viewModel.loginSuccess.observe(this) { user ->
            enableView()
            loginProgress.visibility = View.GONE

            user?.let {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

        viewModel.loginError.observe(this) { errorMessage ->
            enableView()
            loginProgress.visibility = View.GONE

            errorMessage?.let {
                CustomDialog.show(
                    context = this,
                    iconRes = R.drawable.ic_outline_cancel_presentation_24,
                    title = "Login Gagal!",
                    message = errorMessage,
                    primaryButtonText = "OK",
                    primaryButtonBackgroundRes = R.drawable.color_button_red,
                    iconColor = R.color.red
                )
            }
        }
    }

    private fun getFCMToken(onTokenReceived: (String?) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "FCM Token: $token")
                onTokenReceived(token)
            } else {
                Log.w("FCM", "Fetching FCM token failed", task.exception)
                onTokenReceived(null)
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun showEmailInputDialog(context: Context) {
        val inputView = LayoutInflater.from(context).inflate(R.layout.dialog_input_email, null)
        val etEmail = inputView.findViewById<EditText>(R.id.etEmailInput)
        val btnPrimary = inputView.findViewById<Button>(R.id.btnPrimary)
        val btnSecondary = inputView.findViewById<Button>(R.id.btnSecondary)
        val icon = inputView.findViewById<ImageView>(R.id.dialogIcon)
        val progressBar = inputView.findViewById<ProgressBar>(R.id.btnProgressBar)

        btnSecondary.backgroundTintList = null
        btnPrimary.backgroundTintList = null
        icon.setImageResource(R.drawable.ic_outline_email_24)

        val dialog = AlertDialog.Builder(context)
            .setView(inputView)
            .setCancelable(false) // tidak bisa dismiss via tap luar/back saat loading
            .create()

        dialog.setCanceledOnTouchOutside(false) // tidak bisa dismiss via tap luar
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        fun setLoading(isLoading: Boolean) {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnPrimary.isEnabled = !isLoading
            btnSecondary.isEnabled = !isLoading
            btnSecondary.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
            dialog.setCancelable(!isLoading)
            dialog.setCanceledOnTouchOutside(!isLoading)
        }

        btnPrimary.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                setLoading(true)
                etEmail.isEnabled = false
                viewModel.forgotPassword(email)
            } else {
                Toast.makeText(context, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }

        btnSecondary.setOnClickListener {
            dialog.dismiss()
        }

        viewModel.forgotPasswordSuccess.observeForever { message ->
            message?.let {
                setLoading(false)
                etEmail.isEnabled = true
                dialog.dismiss()
                CustomDialog.show(
                    context = context,
                    iconRes = R.drawable.ic_outline_check_circle_24,
                    iconColor = R.color.blue,
                    title = "Berhasil!",
                    message = it,
                    primaryButtonText = "OK",
                    primaryButtonBackgroundRes = R.drawable.color_button_blue
                )
                viewModel.clearForgotPasswordState()
            }
        }

        viewModel.forgotPasswordError.observeForever { error ->
            error?.let {
                setLoading(false)
                etEmail.isEnabled = true
                CustomDialog.show(
                    context = context,
                    iconRes = R.drawable.ic_outline_cancel_presentation_24,
                    iconColor = R.color.red,
                    title = "Gagal!",
                    message = it,
                    primaryButtonText = "OK",
                    primaryButtonBackgroundRes = R.drawable.color_button_red
                )
                viewModel.clearForgotPasswordState()
            }
        }

        dialog.show()
    }

}