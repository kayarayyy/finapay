package com.example.finapay.ui.my_account

import android.Manifest
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
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.finapay.ui.home.HomeViewModel
import com.example.finapay.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class MyAccountActivity : AppCompatActivity(){
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var locationTextView: TextView
    private lateinit var locationErrorTextView: TextView
    private lateinit var refreshLocationButton: MaterialButton

    private lateinit var ttlInput: TextInputEditText
    private lateinit var salaryInput: TextInputEditText
    private var isEditing = false

    private val LOCATION_PERMISSION_CODE = 1001
    private val REQUEST_IMAGE_PICK = 2002
    private val CAMERA_PERMISSION_CODE = 1002

    private var imageUri: Uri? = null
    private var currentLat: Double? = null
    private var currentLon: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_account)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationTextView = findViewById(R.id.location_value)
        locationErrorTextView = findViewById(R.id.location_error)
        refreshLocationButton = findViewById(R.id.refresh_location_button)
        ttlInput = findViewById(R.id.ttl_input)
        salaryInput = findViewById(R.id.salary_input)

        refreshLocationButton.setOnClickListener {
            checkLocationPermissionAndFetch()
        }

        ttlInput.setOnClickListener {
            showDatePickerDialog()
        }

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

        viewModel.customerDetailsSuccess.observe(this) { customer ->
            Toast.makeText(this, "Berhasil Memuat data", Toast.LENGTH_LONG).show()
        }

        viewModel.customerDetailsError.observe(this) { error ->
            Toast.makeText(this, "Gagal memuat data: $error", Toast.LENGTH_LONG).show()
        }

        viewModel.getCustomerDetails()
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
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
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

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private fun getLastLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
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
        val options = arrayOf("Ambil dari Kamera", "Pilih dari Galeri")
        AlertDialog.Builder(this)
            .setTitle("Upload KTP")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
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
        cameraLauncher.launch(imageUri!!)
    }

    private fun createImageUri(): Uri {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "profile_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
    }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess && imageUri != null) {
//                ktpPreview.setImageURI(imageUri)
//                previewCard.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "Pengambilan foto gagal", Toast.LENGTH_SHORT).show()
            }
        }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            } else {
                showLocationError("Izin lokasi ditolak, tidak dapat mengambil lokasi.")
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
            year,
            month,
            day
        )

        // Batas tahun untuk validasi usia, misalnya minimal lahir tahun 1960 dan maksimal sekarang
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

}