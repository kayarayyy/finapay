package com.example.finapay.ui.simulation

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.finapay.R
import com.example.finapay.ui.adapter.PlafondAdapter
import com.example.finapay.ui.landing.LandingViewModel
import com.facebook.shimmer.ShimmerFrameLayout

class SimulationActivity : AppCompatActivity() {
    private val viewModel: LandingViewModel by viewModels()
    private lateinit var adapter: PlafondAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simulation)

        val recyclerView = findViewById<RecyclerView>(R.id.rv_plafond)
        val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)

        adapter = PlafondAdapter(mutableListOf())

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager

        recyclerView.adapter = adapter

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        val shimmerLayout = findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)
        shimmerLayout.startShimmer()
        shimmerLayout.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        // Observasi data dari ViewModel
        viewModel.plafonds.observe(this) { plafonds ->
            adapter.updateData(plafonds)
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            swipeRefreshLayout.isRefreshing = false
        }

        // Error handling untuk kegagalan memuat data
        viewModel.plafondsError.observe(this) { error ->
            if (!error.isNullOrBlank()) {
                Toast.makeText(this, "Gagal memuat data: $error", Toast.LENGTH_LONG).show()
                shimmerLayout.startShimmer()
                shimmerLayout.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
            swipeRefreshLayout.isRefreshing = false
        }

        viewModel.getPlafonds()

        swipeRefreshLayout.setOnRefreshListener {
            viewModel.getPlafonds()
        }
    }
}