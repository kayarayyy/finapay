package com.example.finapay.data.models

data class LoanModel (
    val id: String,
    val amount: String,
    val instalment: String,
    val tenor: String,
    val title: String,
    val backOfficeDisbursedAt: String,
    val status: String,
    val isApproved: Boolean
)