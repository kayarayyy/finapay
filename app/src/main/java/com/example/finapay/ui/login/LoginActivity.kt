package com.example.finapay.ui.login

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.finapay.MainActivity
import com.example.finapay.R
import com.example.finapay.ui.animation.Animation
import com.example.finapay.ui.register.RegisterActivity
import com.example.finapay.utils.CustomDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException

class LoginActivity : AppCompatActivity(){
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val name = account?.displayName
            val email = account?.email
            Toast.makeText(this, "Login Berhasil " + email + " " + name, Toast.LENGTH_SHORT).show()
        } catch (e: ApiException) {
            Log.e("GoogleLogin", "Login gagal, code=${e.statusCode}", e)
            Toast.makeText(this, "Login gagal", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_FINAPay)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        val emailField = findViewById<EditText>(R.id.etEmail)
        val passwordField = findViewById<EditText>(R.id.etPassword)
        val loginButton = findViewById<Button>(R.id.btnLogin)
        val loginGoogleButton = findViewById<SignInButton>(R.id.btn_google_sign_in)
        val loginProgress = findViewById<ProgressBar>(R.id.btnProgressBar)
        var bgWave = findViewById<ImageView>(R.id.bgWave)
        var bgWave1 = findViewById<ImageView>(R.id.bgWave1)
        val animation = Animation()
        animation.animationslidebottom(bgWave)
        animation.animationslidebottom(bgWave1, 550)

        loginButton.backgroundTintList=null

        val textView = findViewById<TextView>(R.id.tvRegisterPrompt)
        val fullText = "Don't have an account? Register"
        val spannable = SpannableString(fullText)

        val start = fullText.indexOf("Register")
        val end = start + "Register".length

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1024411678802-hs0qibp02i1rhfv4b3vul454006l7mj5.apps.googleusercontent.com") // Ganti dengan Client ID milikmu
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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

        textView.text = spannable
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT


        loginGoogleButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }


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
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

        viewModel.loginError.observe(this) { errorMessage ->
            loginProgress.visibility = View.GONE
            loginButton.text = "Login"
            loginButton.isEnabled = true

            errorMessage?.let {
                CustomDialog.show(
                    context = this,
                    iconRes = R.drawable.ic_baseline_360_24,
                    title = "Login Gagal!",
                    message = errorMessage,
                    buttonText = "OK",
                    buttonBackgroundRes = R.drawable.color_button_red,
                    iconColor = R.color.red
                )
            }
        }

    }

}