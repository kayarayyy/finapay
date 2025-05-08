package com.example.finapay.data.models

data class PlafondModel(
    val plan: String,
    val amount: String,
    val colorStart: String,
    val colorEnd: String,
    val annualRate: Any? = null,
)
