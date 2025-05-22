package com.example.finapay.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class PlafondModel(
    val plan: String,
    val amount: String,
    val colorStart: String,
    val colorEnd: String,
    val annualRate: Double = 0.0,
    val adminRate: Double = 0.025
) : Parcelable
