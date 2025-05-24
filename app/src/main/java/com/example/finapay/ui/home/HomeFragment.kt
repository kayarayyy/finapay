package com.example.finapay.ui.home

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.finapay.R
import com.example.finapay.data.models.CustomerDetailModel
import com.example.finapay.data.models.LoanModel
import com.example.finapay.ui.adapter.ActiveLoanAdapter
import com.example.finapay.ui.adapter.PaymentAdapter
import com.example.finapay.ui.my_account.MyAccountActivity
import com.example.finapay.ui.request.RequestActivity
import com.example.finapay.utils.CustomDialog
import com.example.finapay.utils.SharedPreferencesHelper
import com.facebook.shimmer.ShimmerFrameLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment() : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var adapterActiveLoan: ActiveLoanAdapter
    private lateinit var adapterPayment: PaymentAdapter
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    // UI Components
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var shimmerTotal: ShimmerFrameLayout
    private lateinit var shimmerUsed: ShimmerFrameLayout
    private lateinit var shimmerAvailable: ShimmerFrameLayout
    private lateinit var shimmerActiveLoan: ShimmerFrameLayout
    private lateinit var shimmerNextPayment: ShimmerFrameLayout
    private lateinit var shimmerName: ShimmerFrameLayout
    private lateinit var tvUserName: TextView
    private lateinit var tvTotalPlafond: TextView
    private lateinit var tvUsedPlafond: TextView
    private lateinit var tvAvailablePlafond: TextView
    private lateinit var ivSimulation: ImageView
    private lateinit var ivHistory: ImageView
    private lateinit var ivBill: ImageView
    private lateinit var ivRequest: ImageView
    private lateinit var rvActiveLoan: RecyclerView
    private lateinit var rvNextPayment: RecyclerView
    private lateinit var cardBackground: View

    private var canRequest: Boolean? = false
    private var onInternet: Boolean? = false

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

        initViews(view)
        setupRecyclerViews()
        setupListeners()
        observeViewModel()

        viewModel.getCustomerDetails()
        viewModel.getLoanHistory("approved")
    }

    private fun initViews(view: View) {
        sharedPreferencesHelper = SharedPreferencesHelper(requireContext())
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        adapterActiveLoan = ActiveLoanAdapter(mutableListOf())
        adapterPayment = PaymentAdapter(mutableListOf())

        // Swipe Refresh Layout
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)

        // Shimmer Views
        shimmerTotal = view.findViewById(R.id.shimmer_total)
        shimmerUsed = view.findViewById(R.id.shimmer_used)
        shimmerAvailable = view.findViewById(R.id.shimmer_available)
        shimmerActiveLoan = view.findViewById(R.id.shimmer_active_loan)
        shimmerNextPayment = view.findViewById(R.id.shimmer_next_payment)
        shimmerName = view.findViewById(R.id.shimmer_name)

        // Text Views
        tvUserName = view.findViewById(R.id.tv_user_name)
        tvTotalPlafond = view.findViewById(R.id.tv_total_plafond)
        tvUsedPlafond = view.findViewById(R.id.tv_used_plafond)
        tvAvailablePlafond = view.findViewById(R.id.tv_available_plafond)

        // Image Views (Clickable buttons)
        ivSimulation = view.findViewById(R.id.iv_simulation)
        ivHistory = view.findViewById(R.id.iv_history)
        ivBill = view.findViewById(R.id.iv_bill)
        ivRequest = view.findViewById(R.id.iv_request)

        // Recycler Views
        rvActiveLoan = view.findViewById(R.id.rv_active_loan)
        rvNextPayment = view.findViewById(R.id.rv_next_payment)

        // Card Background
        cardBackground = view.findViewById(R.id.card_background)
    }

    private fun setupRecyclerViews() {
        // Active Loan RecyclerView
        val lmActiveLoan = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        val shActiveLoan = PagerSnapHelper()

        rvActiveLoan.apply {
            layoutManager = lmActiveLoan
            adapter = adapterActiveLoan
        }
        shActiveLoan.attachToRecyclerView(rvActiveLoan)

        // Next Payment RecyclerView
        val lmNextPayment = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        val shNextPayment = PagerSnapHelper()

        rvNextPayment.apply {
            layoutManager = lmNextPayment
            adapter = adapterPayment
        }
        shNextPayment.attachToRecyclerView(rvNextPayment)
    }

    private fun setupListeners() {
        // Swipe Refresh Listener
        swipeRefreshLayout.setOnRefreshListener {
            startShimmerAnimation()
            hideDataViews()
            onInternet = true
            viewModel.getCustomerDetails()
            viewModel.getLoanHistory("approved")
        }

        // Simulation Button Listener
        ivSimulation.setOnClickListener {
            showFeatureNotAvailableDialog("simulasi")
        }

        ivBill.setOnClickListener {
            showFeatureNotAvailableDialog("tagihan")
        }

        ivHistory.setOnClickListener {
            showFeatureNotAvailableDialog("riwayat")
        }

        // Request Button Listener
        ivRequest.setOnClickListener {
            if (onInternet == true) {
                if (canRequest == false) {
                    showDataIncompleteDialog()
                } else {
                    navigateToRequestActivity()
                }
            } else {
                showFeatureNotAvailableDialog("ajukan")
            }
        }
    }

    private fun observeViewModel() {
        viewModel.loanHistorySuccess.observe(viewLifecycleOwner) { loans ->
            stopShimmerActiveLoan()

            updateActiveLoanData(loans)
            updatePaymentData(loans)
        }
        viewModel.loanHistoryError.observe(viewLifecycleOwner) { error ->
            stopShimmerActiveLoan()
        }

        // Success Observer
        viewModel.customerDetailsSuccess.observe(viewLifecycleOwner) { customer ->
            swipeRefreshLayout.isRefreshing = false

            setupCardBackground(customer)
            updateUserInfo(customer)

            stopShimmerAnimation()
            showDataViews()
            canRequest = true
            onInternet = true
        }

        // Error Observer
        viewModel.customerDetailsError.observe(viewLifecycleOwner) { error ->
            swipeRefreshLayout.isRefreshing = false

            stopShimmerAnimation()
            setErrorStateData()
            showDataViews()
            canRequest = false
            onInternet = true
        }

        viewModel.internetErrorError.observe(viewLifecycleOwner) { error ->
            swipeRefreshLayout.isRefreshing = false
            stopShimmerAnimation()
            setErrorStateData()
            showDataViews()
            canRequest = false
            onInternet = false
        }
    }

    private fun startShimmerAnimation() {
        listOf(
            shimmerTotal,
            shimmerUsed,
            shimmerAvailable,
            shimmerActiveLoan,
            shimmerNextPayment,
            shimmerName
        )
            .forEach { shimmer ->
                shimmer.startShimmer()
                shimmer.visibility = View.VISIBLE
            }
    }

    private fun stopShimmerAnimation() {
        listOf(
            shimmerTotal,
            shimmerUsed,
            shimmerAvailable,
            shimmerName
        )
            .forEach { shimmer ->
                shimmer.stopShimmer()
                shimmer.visibility = View.GONE
            }
    }

    private fun stopShimmerActiveLoan() {
        listOf(
            shimmerActiveLoan,
            shimmerNextPayment
        )
            .forEach { shimmer ->
                shimmer.stopShimmer()
                shimmer.visibility = View.GONE
            }
    }

    private fun hideDataViews() {
        tvTotalPlafond.visibility = View.GONE
        tvUsedPlafond.visibility = View.GONE
        tvAvailablePlafond.visibility = View.GONE
        tvUserName.setText("")
        rvActiveLoan.visibility = View.GONE
        rvNextPayment.visibility = View.GONE
    }

    private fun showDataViews() {
        tvTotalPlafond.visibility = View.VISIBLE
        tvUsedPlafond.visibility = View.VISIBLE
        tvAvailablePlafond.visibility = View.VISIBLE
        rvActiveLoan.visibility = View.VISIBLE
        rvNextPayment.visibility = View.VISIBLE
    }

    private fun setupCardBackground(customer: CustomerDetailModel) {
        try {
            val colors = intArrayOf(
                Color.parseColor(customer.plafond?.colorStart),
                Color.parseColor(customer.plafond?.colorEnd)
            )
            val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                colors
            ).apply {
                cornerRadius = 32f
            }
            cardBackground.background = gradientDrawable
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    private fun updateActiveLoanData(loanModel: List<LoanModel>) {
        adapterActiveLoan.updateData(loanModel)
    }

    private fun updatePaymentData(paymentModel: List<LoanModel>) {
        adapterPayment.updateData(paymentModel)
    }

    private fun updateUserInfo(customer: CustomerDetailModel) {
        tvTotalPlafond.setText(customer.plafond?.plan)
        tvUsedPlafond.setText("Terpakai: ${customer.usedPlafond}")
        tvAvailablePlafond.setText(customer.availablePlafond)
        tvUserName.setText(customer.user?.name)
    }

    private fun setErrorStateData() {
        tvTotalPlafond.setText("-")
        tvUsedPlafond.setText("Terpakai: Rp -")
        tvAvailablePlafond.setText("Rp -")

        val user = sharedPreferencesHelper.getUserData()
        tvUserName.setText(user?.name ?: "-")
    }

    private fun showFeatureNotAvailableDialog(name: String) {
        CustomDialog.show(
            context = requireContext(),
            iconRes = R.drawable.ic_baseline_error_outline_24,
            title = "Fitur Belum Tersedia",
            message = "Fitur ${name} pinjaman saat ini belum tersedia. Silakan coba lagi nanti.",
            primaryButtonText = "Oke",
            primaryButtonBackgroundRes = R.drawable.color_button_blue,
            iconColor = R.color.red,
        )
    }

    private fun navigateToRequestActivity() {
        val intent = Intent(requireContext(), RequestActivity::class.java)
        intent.putExtra("customer", viewModel.customerDetailsSuccess.value)
        startActivity(intent)
    }

    private fun showDataIncompleteDialog() {
        CustomDialog.show(
            context = requireContext(),
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

    private fun goToMyAccount() {
        val intent = Intent(requireContext(), MyAccountActivity::class.java)
        startActivity(intent)
    }
}