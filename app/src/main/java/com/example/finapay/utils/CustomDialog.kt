package com.example.finapay.utils

import android.app.AlertDialog
import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.finapay.R


object CustomDialog {
    fun show(
        context: Context,
        iconRes: Int,
        iconColor: Int? = null,
        title: String,
        message: String,
        primaryButtonText: String? = null,
        primaryButtonBackgroundRes: Int? = null,
        onPrimaryClick: (() -> Unit)? = null,
        secondaryButtonText: String? = null,
        secondaryButtonBackgroundRes: Int? = null,
        onSecondaryClick: (() -> Unit)? = null,
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.custom_dialog, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val icon = dialogView.findViewById<ImageView>(R.id.dialogIcon)
        val tvTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val tvMessage = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val btnPrimary = dialogView.findViewById<Button>(R.id.btnPrimary)
        val btnSecondary = dialogView.findViewById<Button>(R.id.btnSecondary)

        // Clear any previous tints
        icon.clearColorFilter()
        icon.backgroundTintList = null

        // Set the icon drawable
        icon.setImageResource(iconRes)

        // Apply color tint if provided
        iconColor?.let {
            // Use ContextCompat to get the color from the resource
            val colorValue = ContextCompat.getColor(context, it)
            icon.setColorFilter(colorValue, PorterDuff.Mode.SRC_IN)
        }

        tvTitle.text = title
        tvMessage.text = message

        // Primary button setup
        if (!primaryButtonText.isNullOrEmpty()) {
            btnPrimary.text = primaryButtonText
            primaryButtonBackgroundRes?.let {
                btnPrimary.backgroundTintList = null
                btnPrimary.background = ContextCompat.getDrawable(context, it)
            }
            btnPrimary.setOnClickListener {
                dialog.dismiss()
                onPrimaryClick?.invoke()
            }
        } else {
            btnPrimary.text = "OK"
            btnPrimary.setOnClickListener { dialog.dismiss() }
        }

        // Secondary button setup
        if (!secondaryButtonText.isNullOrEmpty()) {
            btnSecondary.visibility = View.VISIBLE
            btnSecondary.text = secondaryButtonText
            secondaryButtonBackgroundRes?.let {
                btnSecondary.backgroundTintList = null
                btnSecondary.background = ContextCompat.getDrawable(context, it)
            }
            btnSecondary.setOnClickListener {
                dialog.dismiss()
                onSecondaryClick?.invoke()
            }
        } else {
            btnSecondary.visibility = View.GONE
        }

        dialog.show()
    }
}