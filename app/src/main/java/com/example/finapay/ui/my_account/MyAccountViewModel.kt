package com.example.finapay.ui.my_account

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finapay.data.models.CustomerDetailModel
import com.example.finapay.data.models.response.ApiResponse
import com.example.finapay.data.repositories.CustomerDetailsRepository
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class MyAccountViewModel : ViewModel() {
    private val repository = CustomerDetailsRepository()

    private val _customerDetails = MutableLiveData<CustomerDetailModel>()
    val customerDetails: LiveData<CustomerDetailModel> = _customerDetails

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun submitCustomerDetails(
        customerDetail: CustomerDetailModel,
        context: Context,
        selfieUri: Uri?,
        ktpUri: Uri?,
        houseUri: Uri?
    ) {
        _isLoading.postValue(true)

        fun String.toRequestBody() = toRequestBody("text/plain".toMediaTypeOrNull())

        fun uriToMultipart(name: String, uri: Uri?, context: Context): MultipartBody.Part? {
            uri ?: return null
            return try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                // Kompresi bitmap ke JPEG dalam memory
                val stream = ByteArrayOutputStream()
                var quality = 90
                var byteArray: ByteArray

                do {
                    stream.reset()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
                    byteArray = stream.toByteArray()
                    quality -= 10
                } while (byteArray.size > 5_000_000 && quality > 10) // Kompres sampai di bawah 5MB atau quality 10

                val requestFile = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
                MultipartBody.Part.createFormData(name, "$name.jpg", requestFile)
            } catch (e: Exception) {
                null
            }
        }


        val latitude = customerDetail.latitude?.toString() ?: ""
        val longitude = customerDetail.longitude?.toString() ?: ""

        if (latitude.isBlank() || longitude.isBlank()) {
            Toast.makeText(context, "Silakan perbarui lokasi terlebih dahulu", Toast.LENGTH_SHORT)
                .show()
            _isLoading.postValue(false)
            return
        }

        val ktpPart = uriToMultipart("ktp", ktpUri, context)
        val selfiePart = uriToMultipart("selfieKtp", selfieUri, context)
        val housePart = uriToMultipart("house", houseUri, context)

        if (ktpPart == null || selfiePart == null || housePart == null) {
            Toast.makeText(context, "Semua gambar harus diisi", Toast.LENGTH_SHORT).show()
            _isLoading.postValue(false)
            return
        }

        viewModelScope.launch {
            try {
                repository.createCustomerDetails(
                    customerDetail.street!!.toRequestBody(),
                    customerDetail.district!!.toRequestBody(),
                    customerDetail.province!!.toRequestBody(),
                    customerDetail.postalCode!!.toRequestBody(),
                    latitude.toRequestBody(),
                    longitude.toRequestBody(),
                    customerDetail.gender!!.toRequestBody(),
                    customerDetail.ttl!!.toRequestBody(),
                    customerDetail.phone!!.toRequestBody(),
                    customerDetail.nik!!.toRequestBody(),
                    customerDetail.mothersName!!.toRequestBody(),
                    customerDetail.job!!.toRequestBody(),
                    customerDetail.salary!!.toRequestBody(),
                    customerDetail.account!!.toRequestBody(),
                    customerDetail.houseStatus!!.toRequestBody(),
                    selfiePart,
                    housePart,
                    ktpPart
                ).enqueue(object : Callback<ApiResponse<CustomerDetailModel>> {
                    override fun onResponse(
                        call: Call<ApiResponse<CustomerDetailModel>>,
                        response: Response<ApiResponse<CustomerDetailModel>>
                    ) {
                        _isLoading.postValue(false)
                        if (response.isSuccessful) {
                            response.body()?.let {
                                _customerDetails.postValue(it.data)
                            } ?: _error.postValue("Terjadi kesalahan: Response body kosong")
                        } else {
                            handleErrorResponse(response)
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse<CustomerDetailModel>>,
                        t: Throwable
                    ) {
                        _isLoading.postValue(false)
                        Log.e(TAG, "API call failure", t)
                        _error.postValue(getErrorMessage(t))
                    }
                })
            } catch (e: Exception) {
                _isLoading.postValue(false)
                Log.e(TAG, "Exception during API call", e)
                _error.postValue("Terjadi kesalahan: ${e.message}")
            }
        }
    }

    private fun handleErrorResponse(response: Response<ApiResponse<CustomerDetailModel>>) {
        val errorBody = response.errorBody()?.string()
        val errorMessage = when (response.code()) {
            400 -> "Data yang dikirim tidak valid"
            401 -> "Sesi telah berakhir, silakan login kembali"
            403 -> "Anda tidak memiliki akses untuk fitur ini"
            404 -> "Layanan tidak ditemukan"
            500, 502, 503 -> "Terjadi gangguan pada server, coba lagi nanti"
            else -> "Terjadi kesalahan: ${response.code()}"
        }

        Log.e(TAG, "API error: ${response.code()} - $errorBody")
        _error.postValue(errorMessage)
    }

    private fun getErrorMessage(throwable: Throwable): String {
        return when {
            throwable.message?.contains("timeout", ignoreCase = true) == true ->
                "Koneksi timeout, periksa jaringan Anda"
            throwable.message?.contains("Unable to resolve host", ignoreCase = true) == true ->
                "Tidak dapat terhubung ke server, periksa koneksi internet Anda"
            else -> "Terjadi kesalahan: ${throwable.message}"
        }
    }

    companion object {
        private const val TAG = "MyAccountViewModel"
    }
}
