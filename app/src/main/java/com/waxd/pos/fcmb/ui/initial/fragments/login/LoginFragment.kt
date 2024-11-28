package com.waxd.pos.fcmb.ui.initial.fragments.login

import android.os.Bundle
import android.view.View
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.base.BaseFragment
import com.waxd.pos.fcmb.databinding.FragmentLoginBinding
import com.waxd.pos.fcmb.ui.initial.InitialActivity

class LoginFragment : BaseFragment<FragmentLoginBinding>() {

    override fun getLayoutRes(): Int = R.layout.fragment_login


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAdded) init()
    }

    override fun init() {
        binding.btnLogin.setOnClickListener {
            (activity as InitialActivity?)?.startMainActivity()
        }
    }

}