package com.example.finapay.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finapay.R
import com.example.finapay.data.models.LoanModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PaymentAdapter(private var items: MutableList<LoanModel>) :
    RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder>() {

    inner  class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val amount = itemView.findViewById<TextView>(R.id.tv_payment_amount)
        val dueDate = itemView.findViewById<TextView>(R.id.tv_due_date)
        val payNow = itemView.findViewById<Button>(R.id.btn_pay_now)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_next_payment, parent, false)

        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val item = items[position]
        holder.amount.text = item.instalment

        val inputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id"))
        val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id"))

        val disbursedDate = inputFormat.parse(item.backOfficeDisbursedAt)
        val today = Calendar.getInstance()

        val resultDate = Calendar.getInstance().apply {
            time = disbursedDate!!

            if (disbursedDate.before(today.time) || disbursedDate == today.time) {
                // Set bulan menjadi bulan sekarang
                set(Calendar.MONTH, today.get(Calendar.MONTH))
            } else {
                // Tambahkan 1 bulan dari hari ini
                time = today.time
                add(Calendar.MONTH, 2)
            }
        }

        holder.dueDate.text = outputFormat.format(resultDate.time)
        holder.payNow.backgroundTintList = null
    }

    fun updateData(newItems: List<LoanModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size
}