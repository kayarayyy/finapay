package com.example.finapay.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finapay.R
import com.example.finapay.data.models.LoanModel

class HistoryLoanAdapter(private var items: MutableList<LoanModel>) :
    RecyclerView.Adapter<HistoryLoanAdapter.HistoryLoanViewHolder>() {
    inner class HistoryLoanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val amount = itemView.findViewById<TextView>(R.id.tv_total_loan)
        val tenor = itemView.findViewById<TextView>(R.id.tv_tenor)
        val status = itemView.findViewById<TextView>(R.id.tv_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryLoanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_loan, parent, false)
        return HistoryLoanViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryLoanViewHolder, position: Int) {
        val item = items[position]
        holder.amount.text = item.amount
        holder.tenor.text = item.tenor
        if (item.status == "APPROVED") {
            holder.status.text = "Disetujui"
            holder.status.setBackgroundResource(R.drawable.status_bg_active)
        } else if (item.status == "REJECTED") {
            holder.status.text = "Ditolak"
            holder.status.setBackgroundResource(R.drawable.color_button_red)
        }

    }

    fun updateData(newItems: List<LoanModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size
}