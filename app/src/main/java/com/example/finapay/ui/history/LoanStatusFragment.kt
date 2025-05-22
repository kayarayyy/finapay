package com.example.finapay.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.finapay.R
import com.example.finapay.ui.adapter.HistoryLoanAdapter

enum class LoanStatus {
    APPROVED,
    REJECTED
}

class LoanStatusFragment : Fragment() {

    private lateinit var viewModel: HistoryViewModel

    // View components
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var emptyText: TextView

    // Adapter untuk RecyclerView
    private lateinit var adapter: HistoryLoanAdapter

    // Status peminjaman yang ditampilkan di fragment ini
    private var loanStatus: LoanStatus = LoanStatus.APPROVED

    companion object {
        private const val ARG_STATUS = "loan_status"

        fun newInstance(status: LoanStatus): LoanStatusFragment {
            val fragment = LoanStatusFragment()
            val args = Bundle()
            args.putSerializable(ARG_STATUS, status)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            @Suppress("DEPRECATION")
            loanStatus = it.getSerializable(ARG_STATUS) as LoanStatus
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_loan_status, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initViews(view)
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(requireParentFragment())[HistoryViewModel::class.java]
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.rv_loan_history)
        swipeRefresh = view.findViewById(R.id.swipe_refresh)
        emptyText = view.findViewById(R.id.tv_empty_data)
    }

    private fun setupRecyclerView() {
        adapter = HistoryLoanAdapter(mutableListOf())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupListeners() {
        swipeRefresh.setOnRefreshListener {
            (parentFragment as? HistoryFragment)?.refreshData()
        }
    }

    private fun observeViewModel() {
        viewModel.loanHistory.observe(viewLifecycleOwner) { allLoans ->
            val filteredLoans = allLoans.filter { loan ->
                when (loanStatus) {
                    LoanStatus.APPROVED -> loan.isApproved
                    LoanStatus.REJECTED -> !loan.isApproved
                }
            }

            adapter.updateData(filteredLoans)

            emptyText.visibility = if (filteredLoans.isEmpty()) View.VISIBLE else View.GONE

            if (swipeRefresh.isRefreshing) {
                swipeRefresh.isRefreshing = false
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (!isLoading && swipeRefresh.isRefreshing) {
                swipeRefresh.isRefreshing = false
            }
        }
    }
}