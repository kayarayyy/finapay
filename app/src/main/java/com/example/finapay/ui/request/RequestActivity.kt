package com.example.finapay.ui.request

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.finapay.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class RequestActivity : AppCompatActivity() {

    private val viewModel: RequestViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var locationTextView: TextView
    private lateinit var ktpPreview: ImageView

    private val LOCATION_PERMISSION_CODE = 1001
    private val REQUEST_IMAGE_PICK = 2002

    private var imageUri: Uri? = null
    private var currentLat: Double? = null
    private var currentLon: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationTextView = findViewById(R.id.location_value)
        ktpPreview = findViewById(R.id.ktp_preview)

        val amountInput = findViewById<EditText>(R.id.amount_input)
        val tenorInput = findViewById<EditText>(R.id.tenor_input)

        checkLocationPermissionAndFetch()

        findViewById<Button>(R.id.upload_ktp_button).setOnClickListener {
            showImagePickerDialog()
        }


        findViewById<Button>(R.id.submit_button).setOnClickListener {
            val amount = amountInput.text.toString().trim()
            val tenor = tenorInput.text.toString().trim()

            if (amount.isEmpty() || tenor.isEmpty()) {
                Toast.makeText(this, "Silakan isi jumlah dan tenor", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            imageUri?.let { uri ->
                submitData(uri, amount, tenor)
            } ?: Toast.makeText(this, "Silakan pilih atau ambil gambar KTP", Toast.LENGTH_SHORT).show()
        }

        observeViewModel()
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess && imageUri != null) {
            ktpPreview.setImageURI(imageUri)
        } else {
            Toast.makeText(this, "Pengambilan foto gagal", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), LOCATION_PERMISSION_CODE)
            return
        }
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

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("ktp_image_", ".jpg", context.cacheDir)
        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input?.copyTo(output)
            }
        }
        return tempFile
    }

    private fun submitData(uri: Uri, amount: String, tenor: String) {
        if (currentLat != null && currentLon != null) {
            val file = uriToFile(this, uri)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("ktp_image", file.name, requestFile)

            val latBody = currentLat.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val lonBody = currentLon.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val amountBody = amount.toRequestBody("text/plain".toMediaTypeOrNull())
            val tenorBody = tenor.toRequestBody("text/plain".toMediaTypeOrNull())

            viewModel.uploadKtpWithLocation(amountBody, tenorBody, latBody, lonBody, imagePart)
        } else {
            Toast.makeText(this, "Pastikan lokasi tersedia", Toast.LENGTH_SHORT).show()
        }
    }


    private fun observeViewModel() {
        viewModel.uploadResult.observe(this) { response ->
            Toast.makeText(this, "Upload sukses: ${response.message}", Toast.LENGTH_SHORT).show()
        }

        viewModel.uploadError.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }

        viewModel.isLoading.observe(this) { isLoading ->
            // TODO: tampilkan/hilangkan loading indicator
        }
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

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_IMAGE_PICK) {
            data?.data?.let { uri ->
                imageUri = uri
                ktpPreview.setImageURI(uri)
            }
        }
    }

    private fun checkLocationPermissionAndFetch() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
                locationTextView.text = "Lokasi: $currentLat, $currentLon"
            } else {
                Toast.makeText(this, "Gagal mendapatkan lokasi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLastLocation()
                } else {
                    Toast.makeText(this, "Izin lokasi ditolak", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
