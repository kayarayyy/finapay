package com.example.finapay.ui.home

import android.app.Application
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
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
import com.example.finapay.ui.my_account.MyAccountActivity
import com.example.finapay.ui.request.RequestActivity
import com.example.finapay.ui.simulation.SimulationActivity
import com.example.finapay.utils.CustomDialog
import com.example.finapay.utils.SharedPreferencesHelper
import com.facebook.shimmer.ShimmerFrameLayout

class HomeFragment() : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var adapterActiveLoan: ActiveLoanAdapter
    private lateinit var adapterPayment: PaymentAdapter
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    private var canRequest: Boolean? = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            val controller =
                WindowCompat.getInsetsController(requireActivity().window, requireView())
            controller?.show(WindowInsetsCompat.Type.statusBars())
        }

        sharedPreferencesHelper = SharedPreferencesHelper(requireContext())

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
            if (canRequest == true) {
                val intent = Intent(requireContext(), RequestActivity::class.java)
                startActivity(intent)
            } else {
                CustomDialog.show(
                    context = this@HomeFragment.requireContext(),
                    iconRes = R.drawable.ic_baseline_error_outline_24,
                    title = "Data Belum Lengkap!",
                    message = "Lengkapi data anda untuk mengajukan pinjaman",
                    primaryButtonText = "Lengkapi Data",
                    primaryButtonBackgroundRes = R.drawable.color_button_blue,
                    secondaryButtonText = "Batal",
                    secondaryButtonBackgroundRes = R.drawable.color_button_gray,
                    iconColor = R.color.red,
                    onPrimaryClick = { goToMyAccount() }
                )
            }
        }

//        Layout Manager
        val lmActiveLoan = LinearLayoutManager(
            this@HomeFragment.requireContext(),
            LinearLayoutManager.HORIZONTAL, false
        )
        val lmNextPayment = LinearLayoutManager(
            this@HomeFragment.requireContext(),
            LinearLayoutManager.HORIZONTAL, false
        )

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

        // Observasi hasil dari ViewModel
        viewModel.customerDetailsSuccess.observe(viewLifecycleOwner) { customer ->
            swipeRefreshLayout.isRefreshing = false

            try {
                val colors = intArrayOf(
                    Color.parseColor(customer.plafond?.colorStart),
                    Color.parseColor(customer.plafond?.colorEnd)
                )
                val gradientDrawable = GradientDrawable(
                    GradientDrawable.Orientation.TL_BR, colors
                )
                gradientDrawable.cornerRadius = 32f

                view.findViewById<View>(R.id.card_background)?.background = gradientDrawable
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }

            val loanModel: List<LoanModel> = listOf(
                LoanModel(
                    id = "1",
                    amount = customer.plafond?.amount ?: "Rp0",
                    title = "Peminjaman #1",
                    date = "20 Mei 2025",
                    status = "Disetujui",
                    tenor = "12",
                    isApproved = true
                ),
                LoanModel(
                    id = "2",
                    amount = customer.plafond?.amount ?: "Rp0",
                    title = "Peminjaman #1",
                    date = "20 Mei 2025",
                    status = "Disetujui",
                    tenor = "12",
                    isApproved = true
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
            tvTotalPlafond.setText(customer.plafond?.plan)
            tvTotalPlafond.visibility = View.VISIBLE

            shimmerUsed.stopShimmer()
            shimmerUsed.visibility = View.GONE
            tvUsedPlafond.setText("Terpakai: " + customer.usedPlafond)
            tvUsedPlafond.visibility = View.VISIBLE

            shimmerAvailable.stopShimmer()
            shimmerAvailable.visibility = View.GONE
            tvAvailablePlafond.setText(customer.availablePlafond)
            tvAvailablePlafond.visibility = View.VISIBLE

            shimmerName.stopShimmer()
            shimmerName.visibility = View.GONE
            tvUserName.setText(customer.user?.name)
            canRequest = true
            // TODO: Update UI dengan customer
        }



        viewModel.customerDetailsError.observe(viewLifecycleOwner) { error ->
            swipeRefreshLayout.isRefreshing = false

            shimmerActiveLoan.visibility = View.GONE
            shimmerNextPayment.visibility = View.GONE

            shimmerTotal.stopShimmer()
            shimmerTotal.visibility = View.GONE
            tvTotalPlafond.setText("-")
            tvTotalPlafond.visibility = View.VISIBLE

            shimmerUsed.stopShimmer()
            shimmerUsed.visibility = View.GONE
            tvUsedPlafond.setText("Terpakai: Rp -")
            tvUsedPlafond.visibility = View.VISIBLE

            shimmerAvailable.stopShimmer()
            shimmerAvailable.visibility = View.GONE
            tvAvailablePlafond.setText("Rp -")
            tvAvailablePlafond.visibility = View.VISIBLE

            shimmerName.stopShimmer()
            shimmerName.visibility = View.GONE
            val user = sharedPreferencesHelper.getUserData()
            tvUserName.setText(user?.name ?: "-")
            canRequest = false
        }

        viewModel.getCustomerDetails()
    }

    private fun goToMyAccount() {
        val intent = Intent(requireContext(), MyAccountActivity::class.java)
        startActivity(intent)
    }
}

