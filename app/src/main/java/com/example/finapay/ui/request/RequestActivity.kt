package com.example.finapay.ui.request

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.finapay.MainActivity
import com.example.finapay.R
import com.example.finapay.data.models.CustomerDetailModel
import com.example.finapay.ui.home.HomeViewModel
import com.example.finapay.utils.CustomDialog
import com.example.finapay.utils.FormUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class RequestActivity : AppCompatActivity() {

    private val viewModel: RequestViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var formUtils: FormUtils

    // UI Components
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

    // Preview Components
    private lateinit var previewCard: CardView
    private lateinit var cardBackground: View
    private lateinit var valueLoanAmount: TextView
    private lateinit var valueAdminFee: TextView
    private lateinit var valueInterestRate: TextView
    private lateinit var valueTotalInterest: TextView
    private lateinit var valueAmountReceived: TextView
    private lateinit var valueMonthlyInstallment: TextView
    private lateinit var valueTotalPayment: TextView
    private lateinit var availablePlafond: TextView

    private lateinit var customerDetails: CustomerDetailModel

    private val LOCATION_PERMISSION_CODE = 1001
    private var currentLat: Double? = null
    private var currentLon: Double? = null
    private var isEditing = false
    private var submitButtonEnabled = true

    // Loan calculation constants
//    companion object {
//        private const val ADMIN_FEE_PERCENTAGE = 0.05 // 5%
//        private const val INTEREST_RATE_6_MONTHS = 0.02 // 2% per month
//        private const val INTEREST_RATE_12_MONTHS = 0.018 // 1.8% per month
//        private const val INTEREST_RATE_24_MONTHS = 0.015 // 1.5% per month
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request)

        setupStatusBar()
        initViews()
        setupListeners()
        observeViewModel()
    }

    private fun setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
        }
    }

    private fun initViews() {
        formUtils = FormUtils()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Main UI Components
        availablePlafond = findViewById(R.id.tv_available_plafond)
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

        // Preview Components
        previewCard = findViewById(R.id.preview_card)
        cardBackground = findViewById(R.id.card_background)
        valueLoanAmount = findViewById(R.id.value_loan_amount)
        valueAdminFee = findViewById(R.id.value_admin_fee)
        valueInterestRate = findViewById(R.id.value_interest_rate)
        valueTotalInterest = findViewById(R.id.value_total_interest)
        valueAmountReceived = findViewById(R.id.value_amount_received)
        valueMonthlyInstallment = findViewById(R.id.value_monthly_installment)
        valueTotalPayment = findViewById(R.id.value_total_payment)

        setRadioButtonColor(R.id.tenor_6)
        setRadioButtonColor(R.id.tenor_12)
        setRadioButtonColor(R.id.tenor_24)
        customerDetails = intent.getParcelableExtra<CustomerDetailModel>("customer")!!
        availablePlafond.setText(customerDetails?.availablePlafond ?: "Rp 0")
        setupCardBackground(customerDetails)
        formUtils.clearErrorOnInput(amountInputLayout, amountInput)
    }

    private fun setupCardBackground(customer: CustomerDetailModel?) {
        try {
            val colors = intArrayOf(
                Color.parseColor(customer?.plafond?.colorStart),
                Color.parseColor(customer?.plafond?.colorEnd)
            )
            val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                colors
            ).apply {
                cornerRadius = 32f
            }
            cardBackground.background = gradientDrawable
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
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
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                },
                iconColor = R.color.blue
            )
            enableView()
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

    private fun disableView() {
        refferalInput.isEnabled = false
        amountInput.isEnabled = false
        tenorRadioGroup.isEnabled = false
        tenor6.isEnabled = false
        tenor12.isEnabled = false
        tenor24.isEnabled = false
        refreshLocationButton.isEnabled = false
        submitButtonEnabled = false
    }

    private fun enableView() {
        refferalInput.isEnabled = true
        amountInput.isEnabled = true
        tenorRadioGroup.isEnabled = true
        tenor6.isEnabled = true
        tenor12.isEnabled = true
        tenor24.isEnabled = true
        refreshLocationButton.isEnabled = true
        submitButtonEnabled = true
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
                updateLoanPreview()
            }
        })
        tenorRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            updateRadioButtonColors(checkedId)
            updateLoanPreview()
        }
        refreshLocationButton.setOnClickListener {
            getCurrentLocation()
        }
        submitButton.setOnClickListener {
            if (!validateForm()) return@setOnClickListener
            if (!submitButtonEnabled) return@setOnClickListener
            submitLoanRequest()
        }
    }

    private fun formatToCurrency(input: String): String {
        return try {
            val number = input.toLong()
            val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
            "${formatter.format(number)}"
        } catch (e: NumberFormatException) {
            input
        }
    }

    private fun parseCurrencyToLong(currency: String): Long {
        return try {
            currency.replace("Rp", "").replace(".", "").replace(" ", "").toLong()
        } catch (e: NumberFormatException) {
            0L
        }
    }

    private fun updateRadioButtonColors(checkedId: Int) {
        // Reset all radio buttons to default color
        setRadioButtonColor(R.id.tenor_6, false)
        setRadioButtonColor(R.id.tenor_12, false)
        setRadioButtonColor(R.id.tenor_24, false)

        // Set selected radio button to primary color
        setRadioButtonColor(checkedId, true)
    }

    private fun setRadioButtonColor(radioButtonId: Int, isSelected: Boolean = false) {
        val radioButton = findViewById<RadioButton>(radioButtonId)
        val colorRes = if (isSelected) {
            R.color.blue
        } else {
            R.color.gray
        }

        radioButton.buttonTintList = ContextCompat.getColorStateList(this, colorRes)
        radioButton.setTextColor(ContextCompat.getColor(this, colorRes))
    }

    private fun updateLoanPreview() {
        val amountText = amountInput.text.toString()
        if (amountText.isEmpty()) {
            previewCard.visibility = View.GONE
            return
        }

        val loanAmount = parseCurrencyToLong(amountText)
        if (loanAmount <= 0) {
            previewCard.visibility = View.GONE
            return
        }

        val selectedTenor = getSelectedTenor()
        if (selectedTenor == 0) {
            previewCard.visibility = View.GONE
            return
        }

        calculateAndDisplayLoan(loanAmount, selectedTenor)
        previewCard.visibility = View.VISIBLE
    }

    private fun getSelectedTenor(): Int {
        return when (tenorRadioGroup.checkedRadioButtonId) {
            R.id.tenor_6 -> 6
            R.id.tenor_12 -> 12
            R.id.tenor_24 -> 24
            else -> 0
        }
    }

    private fun calculateAndDisplayLoan(loanAmount: Long, tenor: Int) {
        val adminRate = customerDetails.plafond?.adminRate
        val finalAdminRate = if (adminRate == null || adminRate == 0.0) 0.025 else adminRate
        val adminFee = (loanAmount * finalAdminRate).toLong()
        val amountReceived = loanAmount
        val monthlyInterestRate = (customerDetails.plafond?.annualRate ?: 0.0) / 12
        val totalInterest = (loanAmount * monthlyInterestRate * tenor).toLong()
        val totalPayment = loanAmount + totalInterest + adminFee
        val monthlyInstallment = totalPayment / tenor

        // Update preview values
        valueLoanAmount.text = formatToCurrency(loanAmount.toString())
        valueAdminFee.text = formatToCurrency(adminFee.toString())
        valueInterestRate.text = "${(monthlyInterestRate * 100).format(1)}% per bulan"
        valueTotalInterest.text = formatToCurrency(totalInterest.toString())
        valueAmountReceived.text = formatToCurrency(amountReceived.toString())
        valueMonthlyInstallment.text = formatToCurrency(monthlyInstallment.toString())
        valueTotalPayment.text = formatToCurrency(totalPayment.toString())
    }

    private fun Double.format(digits: Int) = "%.${digits}f".format(this)

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
            return
        }

        showLocationLoading(true)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            showLocationLoading(false)
//            if (location != null) {
                currentLat = location.latitude
                currentLon = location.longitude
                updateLocationDisplay(location.latitude, location.longitude)
                hideLocationError()
//            } else {
//                showLocationError("Tidak dapat mendapatkan lokasi. Pastikan GPS aktif.")
//            }
        }.addOnFailureListener {
            showLocationLoading(false)
            showLocationError("Gagal mendapatkan lokasi: ${it.message}")
        }
    }

    private fun showLocationLoading(show: Boolean) {
        if (show) {
            locationTextView.text = "Mendapatkan lokasi..."
        }
        refreshLocationButton.isEnabled = !show
    }

    private fun updateLocationDisplay(lat: Double, lon: Double) {
        locationTextView.text = "Lat: ${lat.format(6)}, Lon: ${lon.format(6)}"
    }

    private fun showLocationError(message: String) {
        locationErrorTextView.text = message
        locationErrorTextView.visibility = View.VISIBLE
        locationTextView.text = "Lokasi tidak tersedia"
    }

    private fun hideLocationError() {
        locationErrorTextView.visibility = View.GONE
    }

    private fun validateForm(): Boolean {
        window.decorView.clearFocus()

        var isValid = true
        var firstInvalidView: View? = null

        fun validateEditText(layout: TextInputLayout, input: TextInputEditText, message: String) {
            if (input.text.isNullOrBlank()) {
                layout.error = "$message"
                layout.boxStrokeErrorColor =
                    ContextCompat.getColorStateList(this, R.color.error_red)
                if (firstInvalidView == null) firstInvalidView = input
                isValid = false
            } else {
                layout.error = null
            }
        }

        validateEditText(amountInputLayout, amountInput, "Jumlah pinjaman wajib diisi")

        if (tenorRadioGroup.checkedRadioButtonId == -1) {
            isValid = false
            val redColor = ContextCompat.getColorStateList(this, R.color.error_red)
            tenor6.buttonTintList = redColor
            tenor12.buttonTintList = redColor
            tenor24.buttonTintList = redColor
        }

        // Validate location
        if (currentLat == null || currentLon == null) {
            showLocationError("Lokasi harus diisi. Tekan tombol refresh untuk mendapatkan lokasi.")
            isValid = false
        }

        firstInvalidView?.requestFocus()

        return isValid
    }

    private fun submitLoanRequest() {
        val referral = refferalInput.text.toString()
        val amount = amountInput.text.toString()
        val tenor = getSelectedTenor().toString()
        val latitude = currentLat.toString()
        val longitude = currentLon.toString()

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
                viewModel.uploadPengajuan(referral, amount, tenor, latitude, longitude)
            },
            iconColor = R.color.blue,
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation()
                } else {
                    showLocationError("Izin lokasi diperlukan untuk mengajukan pinjaman")
                }
            }
        }
    }
}