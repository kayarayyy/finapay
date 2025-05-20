package com.example.finapay.ui.request

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.finapay.MainActivity
import com.example.finapay.R
import com.example.finapay.ui.home.HomeFragment
import com.example.finapay.utils.CustomDialog
import com.example.finapay.utils.FormUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class RequestActivity : AppCompatActivity() {

    private val viewModel: RequestViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var formUtils: FormUtils

    private lateinit var progressBar: View

    private lateinit var locationTextView: TextView
    private lateinit var locationErrorTextView: TextView

    private lateinit var amountInput: TextInputEditText
    private lateinit var refferalInput: TextInputEditText

    private lateinit var amountInputLayout: TextInputLayout
    private lateinit var refferalInputLayout: TextInputLayout

    private lateinit var tenorRadioGroup: RadioGroup
    private lateinit var tenor6: RadioButton
    private lateinit var tenor12: RadioButton
    private lateinit var tenor24: RadioButton

    private lateinit var refreshLocationButton: MaterialButton
    private lateinit var submitButton: MaterialButton

    private val LOCATION_PERMISSION_CODE = 1001
    private var currentLat: Double? = null
    private var currentLon: Double? = null

    private var submitButtonIsEnabled: Boolean = true
    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
        }

        observeViewModel()
        initViews()
        setupListeners()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun initViews() {
        formUtils = FormUtils()
        locationTextView = findViewById(R.id.location_value)
        locationErrorTextView = findViewById(R.id.location_error)
        amountInput = findViewById(R.id.amount_input)
        refferalInput = findViewById(R.id.referral_input)
        amountInputLayout = findViewById(R.id.amount_layout)
        refferalInputLayout = findViewById(R.id.referral_layout)
        tenorRadioGroup = findViewById(R.id.tenor_group)
        tenor6 = findViewById(R.id.tenor_6)
        tenor12 = findViewById(R.id.tenor_12)
        tenor24 = findViewById(R.id.tenor_24)
        refreshLocationButton = findViewById(R.id.refresh_location_button)
        submitButton = findViewById(R.id.submit_button)
        progressBar = findViewById(R.id.loading_indicator)

        setRadioButtonColor(R.id.tenor_6)
        setRadioButtonColor(R.id.tenor_12)
        setRadioButtonColor(R.id.tenor_24)

        formUtils.clearErrorOnInput(amountInputLayout, amountInput)
    }

    private fun disableView() {
        submitButtonIsEnabled = false
        refferalInput.isEnabled = false
        amountInput.isEnabled = false
        tenorRadioGroup.isEnabled = false
        tenor6.isEnabled = false
        tenor12.isEnabled = false
        tenor24.isEnabled = false
        refreshLocationButton.isEnabled = false
    }

    private fun enableView() {
        submitButtonIsEnabled = true
        refferalInput.isEnabled = true
        amountInput.isEnabled = true
        tenorRadioGroup.isEnabled = true
        tenor6.isEnabled = true
        tenor12.isEnabled = true
        tenor24.isEnabled = true
        refreshLocationButton.isEnabled = true
        submitButton.isEnabled = true
    }

    private fun setRadioButtonColor(id: Int) {
        val radio = findViewById<RadioButton>(id)
        radio.buttonTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue_primary))
    }

    private fun setupListeners() {
        amountInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true
                val input = s.toString().replace(".", "").replace("Rp", "").replace(" ", "")
                if (input.isNotEmpty()) {
                    val formatted = formatToCurrency(input)
                    amountInput.setText(formatted)
                    amountInput.setSelection(formatted.length)
                }
                isEditing = false
            }
        })
        refreshLocationButton.setOnClickListener { checkLocationPermissionAndFetch() }
        submitButton.setOnClickListener {
            if (submitButtonIsEnabled) {
                submitData()
            }
        }
        tenorRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            val blueColor = ContextCompat.getColorStateList(this, R.color.blue)
            val grayColor = ContextCompat.getColorStateList(this, R.color.gray)

            when (checkedId) {
                R.id.tenor_6 -> {
                    tenor6.buttonTintList = blueColor
                    tenor12.buttonTintList = grayColor
                    tenor24.buttonTintList = grayColor
                }
                R.id.tenor_12 -> {
                    tenor6.buttonTintList = grayColor
                    tenor12.buttonTintList = blueColor
                    tenor24.buttonTintList = grayColor
                }
                R.id.tenor_24 -> {
                    tenor6.buttonTintList = grayColor
                    tenor12.buttonTintList = grayColor
                    tenor24.buttonTintList = blueColor
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.uploadResult.observe(this) { response ->
            CustomDialog.show(
                context = this,
                iconRes = R.drawable.ic_baseline_add_task_24,
                title = "Sukses!",
                message = "Pengajuan anda akan di review",
                primaryButtonText = "OK",
                primaryButtonBackgroundRes = R.drawable.color_button_blue,
                onPrimaryClick = {
                    val intent = Intent(this, MainActivity::class.java) // ✅ BENAR
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                },
                iconColor = R.color.blue
            )
        }

        viewModel.uploadError.observe(this) { error ->
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
                progressBar.visibility = View.VISIBLE
                submitButton.text = ""
            } else {
                enableView()
                progressBar.visibility = View.GONE
                submitButton.text = "Submit"
            }
        }
    }

    private fun submitData(){
        if (!validateForm()) return
        val amount = amountInput.text.toString()
        val referral = refferalInput.text.toString()
        val tenor = when (tenorRadioGroup.checkedRadioButtonId) {
            R.id.tenor_6 -> 6
            R.id.tenor_12 -> 12
            R.id.tenor_24 -> 24
            else -> null
        }
        CustomDialog.show(
            context = this,
            iconRes = R.drawable.ic_baseline_assignment_add_24,
            title = "Ajukan Peminjaman?",
            message = "Pastikan data anda sudah terisi dengan benar",
            primaryButtonText = "Ya",
            primaryButtonBackgroundRes = R.drawable.color_button_blue,
            secondaryButtonText = "Batal",
            secondaryButtonBackgroundRes = R.drawable.color_button_gray,
            onPrimaryClick = {
                viewModel.uploadPengajuan(referral, amount, tenor.toString(), currentLat.toString(), currentLon.toString())
            },
            iconColor = R.color.blue,
        )
    }

    private fun validateForm(): Boolean {
        window.decorView.clearFocus()

        var isValid = true
        var firstInvalidView: View? = null

        fun validateEditText(layout: TextInputLayout, input: TextInputEditText, fieldName: String) {
            if (input.text.isNullOrBlank()) {
                layout.error = "$fieldName wajib diisi"
                layout.boxStrokeErrorColor =
                    ContextCompat.getColorStateList(this, R.color.error_red)
                if (firstInvalidView == null) firstInvalidView = input
                isValid = false
            } else {
                layout.error = null
            }
        }

        validateEditText(amountInputLayout, amountInput, "Amount")

        if (tenorRadioGroup.checkedRadioButtonId == -1) {
            isValid = false
            val redColor = ContextCompat.getColorStateList(this, R.color.error_red)
            tenor6.buttonTintList = redColor
            tenor12.buttonTintList = redColor
            tenor24.buttonTintList = redColor
        }

        if (currentLat == null || currentLon == null) {
            locationErrorTextView.visibility = View.VISIBLE
            locationErrorTextView.text = "Lokasi belum dipilih"
            isValid = false
            if (firstInvalidView == null) firstInvalidView = locationTextView
        } else {
            locationErrorTextView.visibility = View.GONE
        }

        firstInvalidView?.requestFocus()

        return isValid
    }

    private fun formatToCurrency(value: String): String {
        return try {
            val parsed = value.toLong()
            String.format("%,d", parsed).replace(",", ".")
        } catch (e: NumberFormatException) {
            ""
        }
    }

    private fun checkLocationPermissionAndFetch() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getLastLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                currentLat = location.latitude
                currentLon = location.longitude
                locationTextView.text = "Lokasi: $currentLat, $currentLon"
                locationTextView.setTextColor(ContextCompat.getColor(this, R.color.black))
                locationTextView.setTypeface(null, Typeface.ITALIC)
                locationErrorTextView.visibility = View.GONE
            } else {
                Toast.makeText(this, "Gagal mendapatkan lokasi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            getLastLocation()
        } else {
            Toast.makeText(this, "Izin lokasi ditolak", Toast.LENGTH_SHORT).show()
        }
    }
}
