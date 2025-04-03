package com.waxd.pos.fcmb.ui.main.fragments.success

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.base.BaseFragment
import com.waxd.pos.fcmb.databinding.FragmentSuccessBinding
import com.waxd.pos.fcmb.utils.constants.Constants


class SuccessFragment : BaseFragment<FragmentSuccessBinding>() {

    override fun getLayoutRes(): Int = R.layout.fragment_success

    override fun getTitle(): String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isAdded) init()
    }

    override fun init() {
        val message = arguments?.getString(Constants.MESSAGE_INTENT) ?: "Success"

        binding.tvMessage.text = message

        binding.tvOkay.setOnClickListener { this.view?.findNavController()?.popBackStack() }
    }

}