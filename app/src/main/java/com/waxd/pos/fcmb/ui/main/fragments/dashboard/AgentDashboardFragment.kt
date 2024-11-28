package com.waxd.pos.fcmb.ui.main.fragments.dashboard

import android.os.Bundle
import android.view.View
import androidx.navigation.findNavController
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.base.BaseFragment
import com.waxd.pos.fcmb.databinding.FragmentAgentDashboardBinding
import com.waxd.pos.fcmb.ui.main.fragments.dashboard.adapter.RecentFarmerActivityAdapter

class AgentDashboardFragment : BaseFragment<FragmentAgentDashboardBinding>() {

    private val recentAdapter by lazy { RecentFarmerActivityAdapter() }

    override fun getLayoutRes(): Int = R.layout.fragment_agent_dashboard

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAdded) {
            init()
        }
    }

    override fun init() {
        binding.rvRecentActivity.adapter = recentAdapter

        binding.viewBgAddFarmer.setOnClickListener {
            this.view?.findNavController()?.navigate(R.id.addFarmerFragment)
        }

        binding.viewBgCreateLoan.setOnClickListener {
            this.view?.findNavController()?.navigate(R.id.createLoanApplicationFragment)
        }

        binding.viewBgListOfFarmers.setOnClickListener {
            this.view?.findNavController()?.navigate(R.id.farmersListFragment)
        }

        binding.viewBgSearchUser.setOnClickListener {
            this.view?.findNavController()?.navigate(R.id.farmersListFragment)
        }

        binding.tvRecentActivity.setOnClickListener {
            this.view?.findNavController()?.navigate(R.id.recentActivityFragment)
        }
    }
}