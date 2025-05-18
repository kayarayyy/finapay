package com.example.finapay.utils

import android.content.Context
import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextWatcher
import androidx.core.content.ContextCompat
import com.example.finapay.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class FormUtils {

    fun setErrorBorder(button: MaterialButton, isError: Boolean, context: Context) {
        val color = if (isError) R.color.error_red else R.color.blue_primary
        val strokeWidth = if (isError) 4 else 2
        button.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(context, color))
        button.strokeWidth = strokeWidth
    }

    fun clearErrorOnInput(layout: TextInputLayout, input: TextInputEditText) {
        input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrBlank()) {
                    layout.error = null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
}
