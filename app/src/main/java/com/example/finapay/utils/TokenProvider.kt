package com.example.finapay.utils

interface TokenProvider {
    fun getToken(): String?
}