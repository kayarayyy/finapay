package com.example.finapay.ui.home

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.finapay.R
import com.example.finapay.data.models.LoanModel
import com.example.finapay.data.models.PaymentModel
import com.example.finapay.ui.adapter.ActiveLoanAdapter
import com.example.finapay.ui.adapter.PaymentAdapter
import com.example.finapay.ui.request.RequestActivity
import com.example.finapay.ui.simulation.SimulationActivity
import com.facebook.shimmer.ShimmerFrameLayout

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var adapterActiveLoan: ActiveLoanAdapter
    private lateinit var adapterPayment: PaymentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)

//        View Model
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

//        Shimmer
        val shimmerTotal = view.findViewById<ShimmerFrameLayout>(R.id.shimmer_total)
        val shimmerUsed = view.findViewById<ShimmerFrameLayout>(R.id.shimmer_used)
        val shimmerAvailable = view.findViewById<ShimmerFrameLayout>(R.id.shimmer_available)
        val shimmerActiveLoan = view.findViewById<ShimmerFrameLayout>(R.id.shimmer_active_loan)
        val shimmerNextPayment = view.findViewById<ShimmerFrameLayout>(R.id.shimmer_next_payment)
        val shimmerName = view.findViewById<ShimmerFrameLayout>(R.id.shimmer_name)

//        Text View
        val tvUserName = view.findViewById<TextView>(R.id.tv_user_name)
        val tvTotalPlafond = view.findViewById<TextView>(R.id.tv_total_plafond)
        val tvUsedPlafond = view.findViewById<TextView>(R.id.tv_used_plafond)
        val tvAvailablePlafond = view.findViewById<TextView>(R.id.tv_available_plafond)

//        Clickable as Button
        val ivSimulation = view.findViewById<ImageView>(R.id.iv_simulation)
        val ivRequest = view.findViewById<ImageView>(R.id.iv_request)
        ivSimulation.setOnClickListener {
            val intent = Intent(requireContext(), SimulationActivity::class.java)
            startActivity(intent)
        }
        ivRequest.setOnClickListener {
            val intent = Intent(requireContext(), RequestActivity::class.java)
            startActivity(intent)
        }

//        Layout Manager
        val lmActiveLoan = LinearLayoutManager(this@HomeFragment.requireContext(),
            LinearLayoutManager.HORIZONTAL, false)
        val lmNextPayment = LinearLayoutManager(this@HomeFragment.requireContext(),
            LinearLayoutManager.HORIZONTAL, false)

//        Snap Helper
        val shActiveLoan = PagerSnapHelper()
        val shNextPayment = PagerSnapHelper()

//        Recycler View
        val rvActiveLoan = view.findViewById<RecyclerView>(R.id.rv_active_loan)
        val rvNextPayment = view.findViewById<RecyclerView>(R.id.rv_next_payment)

//        Adapter init
        adapterActiveLoan = ActiveLoanAdapter(mutableListOf())
        rvActiveLoan.layoutManager = lmActiveLoan
        rvActiveLoan.adapter = adapterActiveLoan
        shActiveLoan.attachToRecyclerView(rvActiveLoan)

        adapterPayment = PaymentAdapter(mutableListOf())
        rvNextPayment.layoutManager = lmNextPayment
        shNextPayment.attachToRecyclerView(rvNextPayment)
        rvNextPayment.adapter = adapterPayment

        swipeRefreshLayout.setOnRefreshListener {
            shimmerTotal.startShimmer()
            shimmerUsed.startShimmer()
            shimmerAvailable.startShimmer()
            shimmerActiveLoan.startShimmer()
            shimmerNextPayment.startShimmer()
            shimmerName.startShimmer()

            shimmerTotal.visibility = View.VISIBLE
            shimmerUsed.visibility = View.VISIBLE
            shimmerAvailable.visibility = View.VISIBLE
            shimmerActiveLoan.visibility = View.VISIBLE
            shimmerNextPayment.visibility = View.VISIBLE
            shimmerName.visibility = View.VISIBLE

            tvTotalPlafond.visibility = View.GONE
            tvUsedPlafond.visibility = View.GONE
            tvAvailablePlafond.visibility = View.GONE
            tvUserName.setText("")


            rvActiveLoan.visibility = View.GONE
            rvNextPayment.visibility = View.GONE
            viewModel.getCustomerDetails()
        }

//        btnSimulation.setOnClickListener {
//            val intent = Intent(requireContext(), SimulationActivity::class.java)
//            startActivity(intent)
//        }

        // Observasi hasil dari ViewModel
        viewModel.customerDetailsSuccess.observe(viewLifecycleOwner) { customer ->
            swipeRefreshLayout.isRefreshing = false

            val loanModel: List<LoanModel> = listOf(
                LoanModel(
                    amount = customer.plafond?.amount ?: "Rp0",
                    tenor = "12"
                ),
                LoanModel(
                    amount = customer.plafond?.amount ?: "Rp0",
                    tenor = "6"
                ),
            )
            adapterActiveLoan.updateData(loanModel)
            shimmerActiveLoan.visibility = View.GONE
            rvActiveLoan.visibility = View.VISIBLE

            val paymentModel: List<PaymentModel> = listOf(
                PaymentModel(
                    amount = "Rp200.000",
                    dueDate = "13 Maret 2025"
                ),
                PaymentModel(
                    amount = "Rp350.000",
                    dueDate = "21 Maret 2025"
                )
            )
            adapterPayment.updateData(paymentModel)
            shimmerNextPayment.visibility = View.GONE
            rvNextPayment.visibility = View.VISIBLE

            shimmerTotal.stopShimmer()
            shimmerTotal.visibility = View.GONE
            tvTotalPlafond.setText(customer.plafond?.amount)
            tvTotalPlafond.visibility = View.VISIBLE

            shimmerUsed.stopShimmer()
            shimmerUsed.visibility = View.GONE
            tvUsedPlafond.setText("Terpakai: " + customer.usedPlafond)
            tvUsedPlafond.visibility = View.VISIBLE

            shimmerAvailable.stopShimmer()
            shimmerAvailable.visibility = View.GONE
            tvAvailablePlafond.setText("Tersedia: " + customer.availablePlafond)
            tvAvailablePlafond.visibility = View.VISIBLE

            shimmerName.stopShimmer()
            shimmerName.visibility = View.GONE
            tvUserName.setText(customer.user?.name)
            // TODO: Update UI dengan customer
        }

        viewModel.customerDetailsError.observe(viewLifecycleOwner) { error ->
            swipeRefreshLayout.isRefreshing = false

            shimmerActiveLoan.visibility = View.GONE
            shimmerNextPayment.visibility = View.GONE

            shimmerTotal.stopShimmer()
            shimmerTotal.visibility = View.GONE
            tvTotalPlafond.setText("Rp -")
            tvTotalPlafond.visibility = View.VISIBLE

            shimmerUsed.stopShimmer()
            shimmerUsed.visibility = View.GONE
            tvUsedPlafond.setText("Terpakai: Rp -")
            tvUsedPlafond.visibility = View.VISIBLE

            shimmerAvailable.stopShimmer()
            shimmerAvailable.visibility = View.GONE
            tvAvailablePlafond.setText("Tersedia: Rp -")
            tvAvailablePlafond.visibility = View.VISIBLE

            shimmerName.stopShimmer()
            shimmerName.visibility = View.GONE
            tvUserName.setText("-")
            // TODO: Tampilkan pesan error, misal pakai Toast
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }

        viewModel.getCustomerDetails()
    }
}

