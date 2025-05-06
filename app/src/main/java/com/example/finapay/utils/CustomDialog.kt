package com.example.finapay.utils

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.finapay.R

object CustomDialog {
    fun show(
        context: Context,
        iconRes: Int,
        iconColor: Int?,
        title: String,
        message: String,
        buttonText: String,
        buttonBackgroundRes: Int, // ini drawable, bukan warna
        onClick: (() -> Unit)? = null
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.custom_dialog, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val icon = dialogView.findViewById<ImageView>(R.id.dialogIcon)
        val tvTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val tvMessage = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val btnAction = dialogView.findViewById<Button>(R.id.btnToLogin)

        btnAction.backgroundTintList = null
        btnAction.background = ContextCompat.getDrawable(context, buttonBackgroundRes)

        val drawable = ContextCompat.getDrawable(context, iconRes)
        icon.setImageDrawable(drawable)

        iconColor?.let {
            drawable?.setTint(it)
        }

        tvTitle.text = title
        tvMessage.text = message
        btnAction.text = buttonText

        btnAction.setOnClickListener {
            dialog.dismiss()
            onClick?.invoke()
        }

        dialog.show()
    }
}
