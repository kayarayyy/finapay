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

class ActiveLoanAdapter(private var items: MutableList<LoanModel>) :
    RecyclerView.Adapter<ActiveLoanAdapter.ActiveLoanViewHolder>() {


    inner class ActiveLoanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val amount = itemView.findViewById<TextView>(R.id.tv_total_loan)
        val tenor = itemView.findViewById<TextView>(R.id.tv_tenor)
        val dueDate = itemView.findViewById<TextView>(R.id.tv_due_date)
        val btnDetail = itemView.findViewById<Button>(R.id.btn_detail_loan)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActiveLoanViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_active_loan, parent, false)
        return ActiveLoanViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActiveLoanViewHolder, position: Int) {
        val item = items[position]
        holder.amount.text = item.amount
        holder.tenor.text = item.tenor
        holder.dueDate.text = item.backOfficeDisbursedAt
        holder.btnDetail.backgroundTintList = null
    }

    fun updateData(newItems: List<LoanModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size
}