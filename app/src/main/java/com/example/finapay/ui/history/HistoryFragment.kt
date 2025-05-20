package com.example.finapay.ui.history

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finapay.R
import com.example.finapay.ui.adapter.HistoryLoanAdapter

class HistoryFragment : Fragment() {

    private lateinit var viewModel: HistoryViewModel
    private lateinit var adapter: HistoryLoanAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: View
    private lateinit var emptyText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tampilkan status bar (jika perlu)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = WindowCompat.getInsetsController(requireActivity().window, view)
            controller?.show(WindowInsetsCompat.Type.statusBars())
        }

        viewModel = ViewModelProvider(this)[HistoryViewModel::class.java]
        recyclerView = view.findViewById(R.id.rv_history_loan)
        progressBar = view.findViewById(R.id.loading_indicator)
        emptyText = view.findViewById(R.id.tv_empty_data)

        adapter = HistoryLoanAdapter(mutableListOf())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        observeViewModel()

        // Ambil data
        viewModel.getLoanHistory()
    }

    private fun observeViewModel() {
        viewModel.loanHistory.observe(viewLifecycleOwner) { loanList ->
            adapter.updateData(loanList)
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Tambahkan logika loading (misal: progress bar)
        }
    }
}
