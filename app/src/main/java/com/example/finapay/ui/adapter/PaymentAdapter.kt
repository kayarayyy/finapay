package com.example.finapay.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finapay.R
import com.example.finapay.data.models.PaymentModel

class PaymentAdapter(private var items: MutableList<PaymentModel>) :
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
        holder.amount.text = item.amount
        holder.dueDate.text = item.dueDate
        holder.payNow.backgroundTintList = null
    }

    fun updateData(newItems: List<PaymentModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size
}