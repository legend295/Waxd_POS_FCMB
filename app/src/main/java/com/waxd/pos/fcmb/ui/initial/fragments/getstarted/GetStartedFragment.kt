package com.waxd.pos.fcmb.ui.initial.fragments.getstarted

import android.os.Bundle
import android.view.View
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.base.BaseFragment
import com.waxd.pos.fcmb.databinding.FragmentGetStartedBinding
import com.waxd.pos.fcmb.utils.TopCropTransformation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GetStartedFragment : BaseFragment<FragmentGetStartedBinding>() {

    override fun getLayoutRes(): Int = R.layout.fragment_get_started

    override fun getTitle(): String =""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAdded) {
           init()
        }
    }

    override fun init() {
        Glide.with(binding.ivSplash)
            .load(R.drawable.ic_get_started)
            .apply(RequestOptions.bitmapTransform(TopCropTransformation()))
            .into(binding.ivSplash)
        binding.btnGetStarted.setOnClickListener {
            this.view?.findNavController()
                ?.navigate(R.id.action_getStartedFragment_to_loginFragment)
        }
    }
}