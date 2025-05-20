package com.example.finapay.ui.landing

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.finapay.R
import com.example.finapay.data.models.PlafondModel
import com.example.finapay.ui.adapter.PlafondAdapter
import com.example.finapay.ui.login.LoginActivity
import com.facebook.shimmer.ShimmerFrameLayout

class LandingActivity : AppCompatActivity() {
    private val viewModel: LandingViewModel by viewModels()
    private lateinit var adapter: PlafondAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_FINAPay)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val recyclerView = findViewById<RecyclerView>(R.id.rv_plafond)
        val tvReloadHint = findViewById<TextView>(R.id.tv_reload_hint)
        val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)

        adapter = PlafondAdapter(mutableListOf())
        tvReloadHint.visibility = View.GONE

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager

        recyclerView.adapter = adapter

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)


        val shimmerLayout = findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)
        shimmerLayout.startShimmer()
        shimmerLayout.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        btnLogin.backgroundTintList = null

        // Observasi data dari ViewModel
        viewModel.plafonds.observe(this) { plafonds ->
            adapter.updateData(plafonds)
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            tvReloadHint.visibility = View.GONE
            swipeRefreshLayout.isRefreshing = false
        }

        // Error handling untuk kegagalan memuat data
        viewModel.plafondsError.observe(this) { error ->
            if (!error.isNullOrBlank()) {
                Toast.makeText(this, "Gagal memuat data: $error", Toast.LENGTH_LONG).show()
                shimmerLayout.startShimmer()
                shimmerLayout.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                tvReloadHint.visibility = View.VISIBLE
            }
            swipeRefreshLayout.isRefreshing = false
        }

        // Panggil data
        viewModel.getPlafonds()

        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        swipeRefreshLayout.setOnRefreshListener {
            viewModel.getPlafonds()
        }

    }
}
