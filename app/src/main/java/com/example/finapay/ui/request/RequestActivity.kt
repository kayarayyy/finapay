package com.example.finapay.ui.request

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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

    private val LOCATION_PERMISSION_CODE = 1001
    private val REQUEST_IMAGE_PICK = 2002

    private var currentLat: Double? = null
    private var currentLon: Double? = null

    private val CAMERA_PERMISSION_CODE = 1002

    private lateinit var ktpPreview: ImageView
    private lateinit var ktpPreviewCard: CardView

    private lateinit var selfiePreview: ImageView
    private lateinit var selfiePreviewCard: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
        }


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationTextView = findViewById(R.id.location_value)

        val amountInput = findViewById<EditText>(R.id.amount_input)
        val tenorRadio = when (findViewById<RadioGroup>(R.id.tenor_group).checkedRadioButtonId) {
            R.id.tenor_6 -> "6"
            R.id.tenor_12 -> "12"
            R.id.tenor_24 -> "24"
            else -> ""
        }
        val rb6 = findViewById<RadioButton>(R.id.tenor_6)
        rb6.buttonTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue_primary))
        val rb12 = findViewById<RadioButton>(R.id.tenor_12)
        rb12.buttonTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue_primary))
        val rb24 = findViewById<RadioButton>(R.id.tenor_24)
        rb24.buttonTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue_primary))


        checkLocationPermissionAndFetch()

        findViewById<Button>(R.id.submit_button).setOnClickListener {

        }
        
        observeViewModel()
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
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLastLocation()
                } else {
                    Toast.makeText(this, "Izin lokasi ditolak", Toast.LENGTH_SHORT).show()
                }
            }

//            CAMERA_PERMISSION_CODE -> {
//                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    launchCamera()
//                } else {
//                    Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
//                }
//            }
        }
    }

}
