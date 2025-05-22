package com.example.finapay.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class UserModel(
    val role: String? = null,
    val nip: String? = null,
    val name: String? = null,
    val active: Boolean? = null,
    val id: String? = null,
    val refferal: String? = null,
    val branch: @RawValue Any? = null,
    val email: String? = null
) : Parcelable

