package com.example.finapay.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.finapay.utils.SharedPreferencesHelper

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferencesHelper = SharedPreferencesHelper(application)

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> = _email

    private val _name = MutableLiveData<String>()
    val name: LiveData<String> = _name

    init {
        loadUserData()
    }

    private fun loadUserData() {
        val user = sharedPreferencesHelper.getUserData()
        _email.value = user?.email ?: ""
        _name.value = user?.name ?: ""
    }
}
