package com.example.finapay.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.finapay.ui.history.LoanStatus
import com.example.finapay.ui.history.LoanStatusFragment

class HistoryPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    // Jumlah tab yang akan ditampilkan
    override fun getItemCount(): Int = 2

    // Buat fragment berdasarkan posisi
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> LoanStatusFragment.newInstance(LoanStatus.APPROVED)
            1 -> LoanStatusFragment.newInstance(LoanStatus.REJECTED)
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }
}