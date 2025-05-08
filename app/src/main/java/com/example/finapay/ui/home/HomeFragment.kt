package com.example.finapay.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.finapay.R

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi ViewModel
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        // Referensi komponen UI
        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        val btnDetailLoan = view.findViewById<Button>(R.id.btn_detail_loan)
        val btnPayNow = view.findViewById<Button>(R.id.btn_pay_now)
        val tvTotalPlafond = view.findViewById<TextView>(R.id.tv_total_plafond)
        val tvUsedPlafond = view.findViewById<TextView>(R.id.tv_used_plafond)
        val tvAvailablePlafond = view.findViewById<TextView>(R.id.tv_available_plafond)
        val tvUserName = view.findViewById<TextView>(R.id.tv_user_name)

        btnDetailLoan.backgroundTintList = null
        btnPayNow.backgroundTintList = null

        // Listener untuk swipe to refresh
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.getCustomerDetails()
        }

        // Observasi hasil dari ViewModel
        viewModel.customerDetailsSuccess.observe(viewLifecycleOwner) { customer ->
            swipeRefreshLayout.isRefreshing = false
            tvTotalPlafond.setText(customer.plafond?.amount)
            tvAvailablePlafond.setText("Tersedia: " + customer.availablePlafond)
            tvUsedPlafond.setText("Terpakai: " + customer.usedPlafond)
            tvUserName.setText(customer.user?.name)
            // TODO: Update UI dengan customer
        }

        viewModel.customerDetailsError.observe(viewLifecycleOwner) { error ->
            swipeRefreshLayout.isRefreshing = false
            tvTotalPlafond.setText("Rp0")
            tvAvailablePlafond.setText("Tersedia: Rp0")
            tvUsedPlafond.setText("Terpakai: Rp0")
            tvUserName.setText("Mr. X")
            // TODO: Tampilkan pesan error, misal pakai Toast
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }

        // Panggil pertama kali
        swipeRefreshLayout.isRefreshing = true
        viewModel.getCustomerDetails()
    }
}

