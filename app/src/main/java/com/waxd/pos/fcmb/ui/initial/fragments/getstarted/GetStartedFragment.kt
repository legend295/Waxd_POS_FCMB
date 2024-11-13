package com.waxd.pos.fcmb.ui.initial.fragments.getstarted

import android.os.Bundle
import android.view.View
import androidx.navigation.findNavController
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.base.BaseFragment
import com.waxd.pos.fcmb.databinding.FragmentGetStartedBinding

class GetStartedFragment : BaseFragment<FragmentGetStartedBinding>() {

    override fun getLayoutRes(): Int = R.layout.fragment_get_started

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAdded) {
            binding.btnGetStarted.setOnClickListener {
                this.view?.findNavController()
                    ?.navigate(R.id.action_getStartedFragment_to_loginFragment)
            }
        }
    }
}