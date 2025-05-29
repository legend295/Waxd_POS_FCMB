package com.waxd.pos.fcmb.ui.main.fragments.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.waxd.fcmb.ui.registration.BVNRegistrationActivity
//import com.waxd.fcmb.ui.registration.BVNRegistrationActivity
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.base.BaseFragment
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.databinding.FragmentAgentDashboardBinding
import com.waxd.pos.fcmb.ui.main.fragments.dashboard.adapter.RecentFarmerActivityAdapter
import com.waxd.pos.fcmb.utils.Util.isInternetAvailable
import com.waxd.pos.fcmb.utils.constants.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AgentDashboardFragment : BaseFragment<FragmentAgentDashboardBinding>() {

    private val viewModel: AgentDashboardViewModel by viewModels()
    private val recentAdapter by lazy { RecentFarmerActivityAdapter() }

    override fun getTitle(): String = ""

    override fun getLayoutRes(): Int = R.layout.fragment_agent_dashboard

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAdded) {
            init()
        }
    }

    override fun init() {
        binding.rvRecentActivity.adapter = recentAdapter
        recentAdapter.clickListener = {
            val bundle = Bundle().apply {
                putString(Constants.IntentKeys.FARMER_ID, it.farmerData?.id)
            }
            this.view?.findNavController()?.navigate(R.id.farmerDetailsFragment, bundle,getNavOptions())
        }
        setObserver()
        if (context?.isInternetAvailable(showMessage = true) == true) {
            viewModel.getRecentFarmers()
        }

        binding.viewBgAddFarmer.setOnClickListener {
            this.view?.findNavController()?.navigate(R.id.createLoanApplicationFragment,
                Bundle(),
                getNavOptions())
        }

        binding.viewBgCreateLoan.setOnClickListener {
            this.view?.findNavController()?.navigate(R.id.loanApplicationsFragment,
                Bundle(),
                getNavOptions())
        }

        binding.viewBgListOfFarmers.setOnClickListener {
            this.view?.findNavController()?.navigate(R.id.farmersListFragment,
                Bundle(),
                getNavOptions())
        }

        binding.viewBgSearchUser.setOnClickListener {
            this.view?.findNavController()?.navigate(R.id.addFarmerFragment,
                Bundle(),
                getNavOptions())
//            startActivity(Intent(requireContext(), BVNRegistrationActivity::class.java))
        }

        binding.tvRecentActivity.setOnClickListener {
            this.view?.findNavController()?.navigate(R.id.recentActivityFragment,
                Bundle(),
                getNavOptions())
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            if (context?.isInternetAvailable(showMessage = true) == true) {
                viewModel.getRecentFarmers()
            }
        }
    }

    private fun setObserver() {
        viewModel.response.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = false
            when (it) {
                is DataResult.Failure -> {}
                DataResult.Loading -> {}
                is DataResult.Success -> {
                    recentAdapter.setList(it.data)
                }
            }
        }
    }
}