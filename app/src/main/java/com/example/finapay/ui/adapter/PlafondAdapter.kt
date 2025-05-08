package com.example.finapay.ui.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finapay.R
import com.example.finapay.data.models.PlafondModel

class PlafondAdapter(private var items: MutableList<PlafondModel>) :
    RecyclerView.Adapter<PlafondAdapter.PlafondViewHolder>() {

    inner class PlafondViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.tv_plafond_name)
        val amount = itemView.findViewById<TextView>(R.id.tv_plafond_amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlafondViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plafond, parent, false)
        return PlafondViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlafondViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.plan
        holder.amount.text = item.amount

        try {
            val colors = intArrayOf(
                Color.parseColor(item.colorStart),
                Color.parseColor(item.colorEnd)
            )
            val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TL_BR, colors
            )
            gradientDrawable.cornerRadius = 32f

            holder.itemView.findViewById<View>(R.id.card_background)?.background = gradientDrawable
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    fun updateData(newItems: List<PlafondModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size
}
