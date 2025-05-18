package com.example.finapay.ui.my_account

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
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
import com.example.finapay.R
import com.example.finapay.data.models.CustomerDetailModel
import com.example.finapay.utils.CustomDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.example.finapay.utils.FormUtils
import java.util.Calendar

class MyAccountActivity : AppCompatActivity() {

    // ViewModel
    private val viewModel: MyAccountViewModel by viewModels()

    // Utils
    private lateinit var formUtils: FormUtils

    // Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_CODE = 1001

    // Views: TextView
    private lateinit var locationTextView: TextView
    private lateinit var locationErrorTextView: TextView

    // Views: EditText
    private lateinit var nikInput: TextInputEditText
    private lateinit var ttlInput: TextInputEditText
    private lateinit var phoneInput: TextInputEditText
    private lateinit var mothersNameInput: TextInputEditText
    private lateinit var jobInput: TextInputEditText
    private lateinit var salaryInput: TextInputEditText
    private lateinit var accountInput: TextInputEditText
    private lateinit var houseStatusInput: TextInputEditText
    private lateinit var streetInput: TextInputEditText
    private lateinit var districtInput: TextInputEditText
    private lateinit var provinceInput: TextInputEditText
    private lateinit var postalCodeInput: TextInputEditText

    //    Views: InputLayout
    private lateinit var nikInputLayout: TextInputLayout
    private lateinit var ttlInputLayout: TextInputLayout
    private lateinit var phoneInputLayout: TextInputLayout
    private lateinit var mothersNameInputLayout: TextInputLayout
    private lateinit var jobInputLayout: TextInputLayout
    private lateinit var salaryInputLayout: TextInputLayout
    private lateinit var accountInputLayout: TextInputLayout
    private lateinit var houseStatusInputLayout: TextInputLayout
    private lateinit var streetInputLayout: TextInputLayout
    private lateinit var districtInputLayout: TextInputLayout
    private lateinit var provinceInputLayout: TextInputLayout
    private lateinit var postalCodeInputLayout: TextInputLayout

    // Views: Radio Group
    private lateinit var genderRadioGroup: RadioGroup
    private lateinit var genderMaleRadioButton: RadioButton
    private lateinit var genderFemaleRadioButton: RadioButton

    // Views: Button
    private lateinit var refreshLocationButton: MaterialButton
    private lateinit var uploadKtpButton: MaterialButton
    private lateinit var uploadSelfieButton: MaterialButton
    private lateinit var uploadHouseButton: MaterialButton
    private lateinit var submitButton: MaterialButton

    //    Views : Progress Bar
    private lateinit var progressBar: View

    // Views: ImageView & CardView
    private lateinit var ktpPreview: ImageView
    private lateinit var ktpPreviewCard: CardView
    private lateinit var selfiePreview: ImageView
    private lateinit var selfiePreviewCard: CardView
    private lateinit var housePreview: ImageView
    private lateinit var housePreviewCard: CardView

    // State
    private var isEditing = false
    private val CAMERA_PERMISSION_CODE = 1002
    private var imageUri: Uri? = null
    private var ktpUri: Uri? = null
    private var selfieUri: Uri? = null
    private var houseUri: Uri? = null
    private var currentLat: Double? = null
    private var currentLon: Double? = null

    // Enums
    private enum class ImageType { KTP, SELFIE, HOUSE }

    private var selectedImageType: ImageType? = null

    private var pendingLocationRequest = false
    private var pendingCameraRequest = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_account)

        initViews()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        refreshLocationButton.setOnClickListener { checkLocationPermissionAndFetch() }
        uploadKtpButton.setOnClickListener {
            selectedImageType = ImageType.KTP
            showImagePickerDialog()
        }

        uploadSelfieButton.setOnClickListener {
            selectedImageType = ImageType.SELFIE
            showImagePickerDialog()
        }
        uploadHouseButton.setOnClickListener {
            selectedImageType = ImageType.HOUSE
            showImagePickerDialog()
        }
        ttlInput.setOnClickListener { showDatePickerDialog() }

        salaryInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                isEditing = true
                val input = s.toString().replace(".", "").replace("Rp", "").replace(" ", "")
                if (input.isNotEmpty()) {
                    val formatted = formatToCurrency(input)
                    salaryInput.setText(formatted)
                    salaryInput.setSelection(formatted.length)
                }
                isEditing = false
            }
        })

        submitButton.setOnClickListener {
            submitData()
        }

        observeViewModel()

    }

    private fun observeViewModel() {
        viewModel.error.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
        viewModel.isLoading.observe(this) { isLoading ->
            // TODO: tampilkan/hilangkan loading indicator
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
                submitButton.text = ""
            } else {
                progressBar.visibility = View.GONE
                submitButton.text = "Submit"
            }
        }
        viewModel.customerDetails.observe(this) { customerDetails ->
            // TODO: tampilkan data pelanggan
            Toast.makeText(this, "Berhasil", Toast.LENGTH_SHORT).show()
        }
    }

    private fun submitData() {
        if (!validateForm()) return
        val customerDetail = CustomerDetailModel(
            nik = nikInput.text.toString().trim(),
            gender = when (genderRadioGroup.checkedRadioButtonId) {
                R.id.gender_male -> "LAKI_LAKI"
                R.id.gender_female -> "PEREMPUAN"
                else -> null
            },
            ttl = ttlInput.text.toString().trim(),
            phone = phoneInput.text.toString().trim(),
            mothersName = mothersNameInput.text.toString().trim(),
            job = jobInput.text.toString().trim(),
            salary = salaryInput.text.toString().trim(),
            account = accountInput.text.toString().trim(),
            houseStatus = houseStatusInput.text.toString().trim(),
            street = streetInput.text.toString().trim(),
            district = districtInput.text.toString().trim(),
            province = provinceInput.text.toString().trim(),
            postalCode = postalCodeInput.text.toString().trim(),
            latitude = currentLat,
            longitude = currentLon,
            selfie = selfieUri?.toString(),
            ktp = ktpUri?.toString(),
            house = houseUri?.toString()
        )

        CustomDialog.show(
            context = this,
            iconRes = R.drawable.ic_outline_image_24,
            title = "Submit",
            message = "Pastikan data anda sudah terisi dengan benar",
            primaryButtonText = "Ya",
            primaryButtonBackgroundRes = R.drawable.color_button_blue,
            secondaryButtonText = "Batal",
            secondaryButtonBackgroundRes = R.drawable.color_button_gray,
            onPrimaryClick = {
                viewModel.submitCustomerDetails(
                    customerDetail,
                    this,
                    selfieUri,
                    ktpUri,
                    houseUri
                )
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

        validateEditText(nikInputLayout, nikInput, "NIK")
        validateEditText(ttlInputLayout, ttlInput, "Tanggal lahir")
        validateEditText(phoneInputLayout, phoneInput, "Nomor telepon")
        validateEditText(mothersNameInputLayout, mothersNameInput, "Nama ibu kandung")
        validateEditText(jobInputLayout, jobInput, "Pekerjaan")
        validateEditText(salaryInputLayout, salaryInput, "Gaji")
        validateEditText(accountInputLayout, accountInput, "Nomor rekening")
        validateEditText(houseStatusInputLayout, houseStatusInput, "Status rumah")
        validateEditText(streetInputLayout, streetInput, "Alamat jalan")
        validateEditText(districtInputLayout, districtInput, "Kecamatan")
        validateEditText(provinceInputLayout, provinceInput, "Provinsi")
        validateEditText(postalCodeInputLayout, postalCodeInput, "Kode pos")

        // Gender
        if (genderRadioGroup.checkedRadioButtonId == -1) {
            isValid = false
            val redColor = ContextCompat.getColorStateList(this, R.color.error_red)
            genderMaleRadioButton.buttonTintList = redColor
            genderFemaleRadioButton.buttonTintList = redColor
            if (firstInvalidView == null) firstInvalidView = genderRadioGroup
        }

        // Lokasi
        if (currentLat == null || currentLon == null) {
            locationErrorTextView.visibility = View.VISIBLE
            locationErrorTextView.text = "Lokasi belum dipilih"
            isValid = false
            if (firstInvalidView == null) firstInvalidView = refreshLocationButton
        } else {
            locationErrorTextView.visibility = View.GONE
        }

        // Gambar
        if (ktpUri == null) {
            isValid = false
            formUtils.setErrorBorder(uploadKtpButton, true, this)
            if (firstInvalidView == null) firstInvalidView = uploadKtpButton
        }

        if (selfieUri == null) {
            isValid = false
            formUtils.setErrorBorder(uploadSelfieButton, true, this)
            if (firstInvalidView == null) firstInvalidView = uploadSelfieButton
        }

        if (houseUri == null) {
            isValid = false
            formUtils.setErrorBorder(uploadHouseButton, true, this)
            if (firstInvalidView == null) firstInvalidView = uploadHouseButton
        }

        // Fokuskan ke field pertama yang invalid
        firstInvalidView?.requestFocus()

        return isValid
    }


    private fun initViews() {
        formUtils = FormUtils()
        progressBar = findViewById(R.id.loading_indicator)
        locationTextView = findViewById(R.id.location_value)
        locationErrorTextView = findViewById(R.id.location_error)
        nikInput = findViewById(R.id.nik_input)
        ttlInput = findViewById(R.id.ttl_input)
        phoneInput = findViewById(R.id.phone_input)
        mothersNameInput = findViewById(R.id.mothers_name_input)
        jobInput = findViewById(R.id.job_input)
        salaryInput = findViewById(R.id.salary_input)
        accountInput = findViewById(R.id.account_input)
        houseStatusInput = findViewById(R.id.house_status_input)
        streetInput = findViewById(R.id.street_input)
        districtInput = findViewById(R.id.district_input)
        provinceInput = findViewById(R.id.province_input)
        postalCodeInput = findViewById(R.id.postal_code_input)
        nikInputLayout = findViewById(R.id.nik_layout)
        ttlInputLayout = findViewById(R.id.ttl_layout)
        phoneInputLayout = findViewById(R.id.phone_layout)
        mothersNameInputLayout = findViewById(R.id.mothers_name_layout)
        jobInputLayout = findViewById(R.id.job_layout)
        salaryInputLayout = findViewById(R.id.salary_layout)
        accountInputLayout = findViewById(R.id.account_layout)
        houseStatusInputLayout = findViewById(R.id.house_status_layout)
        streetInputLayout = findViewById(R.id.street_layout)
        districtInputLayout = findViewById(R.id.district_layout)
        provinceInputLayout = findViewById(R.id.province_layout)
        postalCodeInputLayout = findViewById(R.id.postal_code_layout)
        refreshLocationButton = findViewById(R.id.refresh_location_button)
        uploadKtpButton = findViewById(R.id.upload_ktp_button)
        uploadSelfieButton = findViewById(R.id.upload_selfie_ktp_button)
        uploadHouseButton = findViewById(R.id.upload_house_button)
        submitButton = findViewById(R.id.submit_button)
        genderRadioGroup = findViewById(R.id.gender_group)
        genderMaleRadioButton = findViewById(R.id.gender_male)
        genderFemaleRadioButton = findViewById(R.id.gender_female)
        ktpPreview = findViewById(R.id.ktp_preview)
        ktpPreviewCard = findViewById(R.id.ktp_preview_card)
        selfiePreview = findViewById(R.id.selfie_ktp_preview)
        selfiePreviewCard = findViewById(R.id.selfie_ktp_preview_card)
        housePreview = findViewById(R.id.house_preview)
        housePreviewCard = findViewById(R.id.house_preview_card)

        formUtils.clearErrorOnInput(nikInputLayout, nikInput)
        formUtils.clearErrorOnInput(ttlInputLayout, ttlInput)
        formUtils.clearErrorOnInput(phoneInputLayout, phoneInput)
        formUtils.clearErrorOnInput(mothersNameInputLayout, mothersNameInput)
        formUtils.clearErrorOnInput(jobInputLayout, jobInput)
        formUtils.clearErrorOnInput(salaryInputLayout, salaryInput)
        formUtils.clearErrorOnInput(accountInputLayout, accountInput)
        formUtils.clearErrorOnInput(houseStatusInputLayout, houseStatusInput)
        formUtils.clearErrorOnInput(streetInputLayout, streetInput)
        formUtils.clearErrorOnInput(districtInputLayout, districtInput)
        formUtils.clearErrorOnInput(provinceInputLayout, provinceInput)
        formUtils.clearErrorOnInput(postalCodeInputLayout, postalCodeInput)
        genderRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            val blueColor = ContextCompat.getColorStateList(this, R.color.blue)
            val grayColor = ContextCompat.getColorStateList(this, R.color.gray)

            when (checkedId) {
                R.id.gender_male -> {
                    genderMaleRadioButton.buttonTintList = blueColor
                    genderFemaleRadioButton.buttonTintList = grayColor
                }

                R.id.gender_female -> {
                    genderMaleRadioButton.buttonTintList = grayColor
                    genderFemaleRadioButton.buttonTintList = blueColor
                }
            }
        }
    }

//
//    fun formUtils.setErrorBorder(button: MaterialButton, isError: Boolean) {
//        val color = if (isError) R.color.error_red else R.color.blue_primary
//        val strokeWidth = if (isError) 4 else 2
//        button.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this, color))
//        button.strokeWidth = strokeWidth
//    }
//
//    fun clearErrorOnInput(layout: TextInputLayout, input: TextInputEditText) {
//        input.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {
//                if (!s.isNullOrBlank()) {
//                    layout.error = null
//                }
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//        })
//    }


    private fun formatToCurrency(value: String): String {
        return try {
            val parsed = value.toLong()
            String.format("%,d", parsed).replace(",", ".")
        } catch (e: NumberFormatException) {
            ""
        }
    }

    private fun checkLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getLastLocation()
        } else {
            pendingLocationRequest = true
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_CODE
            )
        }
    }


    private fun showPermissionSettingsDialog(permissionName: String) {
        AlertDialog.Builder(this).setTitle("Izin Diperlukan")
            .setMessage("Izin $permissionName diperlukan untuk fitur ini. Aktifkan secara manual di pengaturan aplikasi.")
            .setPositiveButton("Pengaturan") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }.setNegativeButton("Batal", null).show()
    }


    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private fun getLastLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLat = location.latitude
                currentLon = location.longitude
                locationTextView.text = "Lat: $currentLat, Lon: $currentLon"
                locationTextView.setTextColor(ContextCompat.getColor(this, R.color.black))
                locationTextView.setTypeface(null, Typeface.ITALIC)
                locationErrorTextView.visibility = View.GONE
            } else {
                showLocationError("Gagal mendapatkan lokasi, coba tekan 'Perbarui'")
            }
        }.addOnFailureListener {
            showLocationError("Terjadi kesalahan saat mengambil lokasi")
        }
    }

    private fun showLocationError(message: String) {
        locationTextView.text = "Lokasi tidak tersedia"
        locationTextView.setTextColor(ContextCompat.getColor(this, R.color.error_red))
        locationTextView.setTypeface(null, Typeface.ITALIC)
        locationErrorTextView.text = message
        locationErrorTextView.visibility = View.VISIBLE
    }

    private fun showImagePickerDialog() {
        CustomDialog.show(
            context = this,
            iconRes = R.drawable.ic_outline_image_24,
            title = "Upload Gambar!",
            message = "Pilih sumber gambar",
            primaryButtonText = "Ambil dari Kamera",
            primaryButtonBackgroundRes = R.drawable.color_button_blue,
            secondaryButtonText = "Ambil dari Galeri",
            secondaryButtonBackgroundRes = R.drawable.color_button_red,
            iconColor = R.color.blue,
            onPrimaryClick = { openCamera() },
            onSecondaryClick = { openGallery() })
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingCameraRequest = true
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE
            )
        } else {
            launchCamera()
        }
    }


    private fun launchCamera() {
        imageUri = createImageUri()
        imageUri?.let { cameraLauncher.launch(it) }
    }

    private fun createImageUri(): Uri {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
    }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess && imageUri != null) {
                displayImage(imageUri!!)
            } else {
                Toast.makeText(this, "Pengambilan foto gagal", Toast.LENGTH_SHORT).show()
            }
        }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let {
                    imageUri = it
                    displayImage(it)
                }
            }
        }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (pendingLocationRequest) {
                        pendingLocationRequest = false
                        getLastLocation()
                    }
                } else {
                    Toast.makeText(this, "Izin lokasi ditolak", Toast.LENGTH_SHORT).show()
                    showPermissionSettingsDialog("Lokasi")
                }
            }

            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (pendingCameraRequest) {
                        pendingCameraRequest = false
                        launchCamera()
                    }
                } else {
                    Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
                    showPermissionSettingsDialog("Kamera")
                }
            }
        }
    }


    private fun displayImage(uri: Uri) {
        when (selectedImageType) {
            ImageType.KTP -> {
                formUtils.setErrorBorder(uploadKtpButton, false, this)
                ktpUri = uri
                ktpPreview.setImageURI(uri)
                ktpPreviewCard.visibility = View.VISIBLE
            }

            ImageType.SELFIE -> {
                formUtils.setErrorBorder(uploadSelfieButton, false, this)
                selfieUri = uri
                selfiePreview.setImageURI(uri)
                selfiePreviewCard.visibility = View.VISIBLE
            }

            ImageType.HOUSE -> {
                formUtils.setErrorBorder(uploadHouseButton, false, this)
                houseUri = uri
                housePreview.setImageURI(uri)
                housePreviewCard.visibility = View.VISIBLE
            }

            else -> {
                Toast.makeText(this, "Jenis gambar tidak diketahui", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate =
                    String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear)
                ttlInput.setText(formattedDate)
            }, year, month, day
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
}
