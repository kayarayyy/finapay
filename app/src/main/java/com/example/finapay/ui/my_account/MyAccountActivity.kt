package com.example.finapay.ui.my_account

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.finapay.ui.home.HomeViewModel
import com.example.finapay.R
import com.example.finapay.utils.CustomDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class MyAccountActivity : AppCompatActivity() {

    // ViewModel
    private val viewModel: HomeViewModel by viewModels()

    // Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_CODE = 1001

    // Views: TextView
    private lateinit var locationTextView: TextView
    private lateinit var locationErrorTextView: TextView

    // Views: EditText
    private lateinit var ttlInput: TextInputEditText
    private lateinit var salaryInput: TextInputEditText

    // Views: Button
    private lateinit var refreshLocationButton: MaterialButton
    private lateinit var uploadKtpButton: MaterialButton
    private lateinit var uploadSelfieButton: MaterialButton
    private lateinit var uploadHouseButton: MaterialButton

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

        viewModel.customerDetailsSuccess.observe(this) {
            Toast.makeText(this, "Berhasil Memuat data", Toast.LENGTH_LONG).show()
        }

        viewModel.customerDetailsError.observe(this) { error ->
            Toast.makeText(this, "Gagal memuat data: $error", Toast.LENGTH_LONG).show()
        }

        viewModel.getCustomerDetails()
    }

    private fun initViews() {
        locationTextView = findViewById(R.id.location_value)
        locationErrorTextView = findViewById(R.id.location_error)
        ttlInput = findViewById(R.id.ttl_input)
        salaryInput = findViewById(R.id.salary_input)
        refreshLocationButton = findViewById(R.id.refresh_location_button)
        uploadKtpButton = findViewById(R.id.upload_ktp_button)
        uploadSelfieButton = findViewById(R.id.upload_selfie_ktp_button)
        uploadHouseButton = findViewById(R.id.upload_house_button)
        ktpPreview = findViewById(R.id.ktp_preview)
        ktpPreviewCard = findViewById(R.id.ktp_preview_card)
        selfiePreview = findViewById(R.id.selfie_ktp_preview)
        selfiePreviewCard = findViewById(R.id.selfie_ktp_preview_card)
        housePreview = findViewById(R.id.house_preview)
        housePreviewCard = findViewById(R.id.house_preview_card)
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            getLastLocation()
        } else {
            pendingLocationRequest = true
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_CODE)
        }
    }


    private fun showPermissionSettingsDialog(permissionName: String) {
        AlertDialog.Builder(this)
            .setTitle("Izin Diperlukan")
            .setMessage("Izin $permissionName diperlukan untuk fitur ini. Aktifkan secara manual di pengaturan aplikasi.")
            .setPositiveButton("Pengaturan") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Batal", null)
            .show()
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
            onSecondaryClick = { openGallery() }
        )
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            pendingCameraRequest = true
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
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
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
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
                ktpUri = uri
                ktpPreview.setImageURI(uri)
                ktpPreviewCard.visibility = View.VISIBLE
            }
            ImageType.SELFIE -> {
                selfieUri = uri
                selfiePreview.setImageURI(uri)
                selfiePreviewCard.visibility = View.VISIBLE
            }
            ImageType.HOUSE -> {
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
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear)
                ttlInput.setText(formattedDate)
            },
            year, month, day
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
}
