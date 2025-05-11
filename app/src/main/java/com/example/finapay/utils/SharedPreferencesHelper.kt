package com.example.finapay.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.finapay.data.models.AuthModel
import com.example.finapay.data.models.RoleModel

class SharedPreferencesHelper(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_pref", Context.MODE_PRIVATE)

    fun saveUserData(authModel: AuthModel, fcmToken: String) {
        with(sharedPreferences.edit()) {
            putString("fcmToken", fcmToken)
            putString("email", authModel.email)
            putString("name", authModel.name)
            putString("roleName", authModel.role.name)
            putString("roleId", authModel.role.id)
            putString("roleAuthority", authModel.role.authority)
            putString("token", authModel.token)
            putBoolean("is_active", authModel.is_active)
            putStringSet("features", authModel.features.toSet())
            putBoolean("alreadyLogin", true)
            apply()
        }
    }

    fun getUserData(): AuthModel? {
        val email = sharedPreferences.getString("email", null) ?: return null
        val name = sharedPreferences.getString("name", null) ?: return null
        val roleName = sharedPreferences.getString("roleName", null) ?: return null
        val roleId = sharedPreferences.getString("roleId", null) ?: return null
        val roleAuthority = sharedPreferences.getString("roleAuthority", null) ?: return null
        val token = sharedPreferences.getString("token", null) ?: return null
        val isActive = sharedPreferences.getBoolean("is_active", false)
        val featuresSet = sharedPreferences.getStringSet("features", emptySet()) ?: emptySet()

        return AuthModel(
            email = email,
            name = name,
            role = RoleModel(id = roleId,name = roleName, authority = roleAuthority),
            token = token,
            features = featuresSet.toList(),
            is_active = isActive
        )
    }

    fun getFcmToken(): String? {
        return sharedPreferences.getString("fcmToken", null)
    }

    fun clearUserData() {
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
    }

    fun setAlreadyLogin(status: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean("alreadyLogin", status)
            apply()
        }
    }

    fun isAlreadyLogin(): Boolean {
        return sharedPreferences.getBoolean("alreadyLogin", false)
    }

}