package com.example.finapay.data.models

data class LoanModel (
    val id: String,
    val amount: String,
    val tenor: String,
    val title: String,
    val date: String,
    val status: String,
    val isApproved: Boolean
)