package com.example.finapay.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.finapay.R
import com.example.finapay.ui.login.LoginActivity
import com.example.finapay.utils.SharedPreferencesHelper

class ProfileFragment : Fragment() {
    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        val tvName = view.findViewById<TextView>(R.id.tv_name)
        val tvEmail = view.findViewById<TextView>(R.id.tv_email)

        btnLogout.backgroundTintList = null

        // Observasi data dari ViewModel
        viewModel.name.observe(viewLifecycleOwner) { name ->
            tvName.text = name
        }

        viewModel.email.observe(viewLifecycleOwner) { email ->
            tvEmail.text = email
        }

        // Aksi logout
        btnLogout.setOnClickListener {
            val sharedPrefHelper = SharedPreferencesHelper(requireContext())
            val fcmToken = sharedPrefHelper.getFcmToken()

            sharedPrefHelper.clearUserData()

            if (fcmToken != null) {
                viewModel.logout(fcmToken) { success ->
                    goToLogin()
                }
            } else {
                goToLogin()
            }
        }


    }
        private fun goToLogin() {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
}
