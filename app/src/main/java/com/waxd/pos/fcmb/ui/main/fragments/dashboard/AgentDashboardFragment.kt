package com.waxd.pos.fcmb.ui.main.fragments.dashboard

import android.os.Bundle
import android.view.View
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.base.BaseFragment
import com.waxd.pos.fcmb.databinding.FragmentAgentDashboardBinding
import com.waxd.pos.fcmb.ui.main.fragments.dashboard.adapter.RecentFarmerActivityAdapter

class AgentDashboardFragment : BaseFragment<FragmentAgentDashboardBinding>() {

    private val recentAdapter by lazy { RecentFarmerActivityAdapter() }

    override fun getLayoutRes(): Int = R.layout.fragment_agent_dashboard

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvRecentActivity.adapter = recentAdapter
    }

}