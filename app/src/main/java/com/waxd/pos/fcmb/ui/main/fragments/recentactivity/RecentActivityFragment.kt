package com.waxd.pos.fcmb.ui.main.fragments.recentactivity

import android.os.Bundle
import android.view.View
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.base.BaseFragment
import com.waxd.pos.fcmb.databinding.FragmentRecentActivityBinding
import com.waxd.pos.fcmb.ui.main.fragments.dashboard.adapter.RecentFarmerActivityAdapter

class RecentActivityFragment : BaseFragment<FragmentRecentActivityBinding>() {

    private val recentAdapter by lazy { RecentFarmerActivityAdapter() }

    override fun getLayoutRes(): Int = R.layout.fragment_recent_activity


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isAdded) {
           init()
        }
    }

    override fun init() {
        binding.rvRecentActivity.adapter = recentAdapter
    }
}