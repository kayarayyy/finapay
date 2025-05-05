package com.example.finapay.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.finapay.data.models.LoginResponse
import com.example.finapay.data.models.RoleModel

class SharedPreferencesHelper(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_pref", Context.MODE_PRIVATE)

    fun saveUserData(loginResponse: LoginResponse) {
        with(sharedPreferences.edit()) {
            putString("email", loginResponse.email)
            putString("name", loginResponse.name)
            putString("roleName", loginResponse.role.name)
            putString("roleId", loginResponse.role.id)
            putString("roleAuthority", loginResponse.role.authority)
            putString("token", loginResponse.token)
            putBoolean("is_active", loginResponse.is_active)
            putStringSet("features", loginResponse.features.toSet())
            apply()
        }
    }

    fun getUserData(): LoginResponse? {
        val email = sharedPreferences.getString("email", null) ?: return null
        val name = sharedPreferences.getString("name", null) ?: return null
        val roleName = sharedPreferences.getString("roleName", null) ?: return null
        val roleId = sharedPreferences.getString("roleId", null) ?: return null
        val roleAuthority = sharedPreferences.getString("roleAuthority", null) ?: return null
        val token = sharedPreferences.getString("token", null) ?: return null
        val isActive = sharedPreferences.getBoolean("is_active", false)
        val featuresSet = sharedPreferences.getStringSet("features", emptySet()) ?: emptySet()

        return LoginResponse(
            email = email,
            name = name,
            role = RoleModel(id = roleId,name = roleName, authority = roleAuthority),
            token = token,
            features = featuresSet.toList(),
            is_active = isActive
        )
    }

    fun clearUserData() {
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
    }
}