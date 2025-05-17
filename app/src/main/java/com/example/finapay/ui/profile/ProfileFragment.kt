package com.example.finapay.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.finapay.R
import com.example.finapay.ui.login.LoginActivity
import com.example.finapay.ui.my_account.MyAccountActivity
import com.example.finapay.utils.CustomDialog
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
        val layoutAccount = view.findViewById<LinearLayout>(R.id.tv_account)
        val layoutSetting = view.findViewById<LinearLayout>(R.id.tv_settings)


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
            CustomDialog.show(
                context = this@ProfileFragment.requireContext(),
                iconRes = R.drawable.ic_baseline_360_24,
                title = "Logout",
                message = "Anda yakin ingin keluar?!",
                primaryButtonText = "OK",
                primaryButtonBackgroundRes = R.drawable.color_button_red,
                secondaryButtonText = "Batal",
                secondaryButtonBackgroundRes = R.drawable.color_button_gray,
                onPrimaryClick = { logout() },
                iconColor = R.color.red
            )
        }
        layoutAccount.setOnClickListener {
            val intent = Intent(requireContext(), MyAccountActivity::class.java)
            startActivity(intent)
        }
        layoutSetting.setOnClickListener {
            Toast.makeText(this@ProfileFragment.requireContext(), "Pengaturan diklik", Toast.LENGTH_SHORT).show()
        }

    }

    private fun logout() {
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

    private fun goToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
