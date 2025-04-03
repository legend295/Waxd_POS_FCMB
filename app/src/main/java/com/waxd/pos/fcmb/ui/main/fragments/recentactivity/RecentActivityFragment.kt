package com.waxd.pos.fcmb.ui.main.fragments.recentactivity

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.base.BaseFragment
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.databinding.FragmentRecentActivityBinding
import com.waxd.pos.fcmb.ui.main.fragments.dashboard.adapter.RecentFarmerActivityAdapter
import com.waxd.pos.fcmb.utils.constants.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecentActivityFragment : BaseFragment<FragmentRecentActivityBinding>() {

    private val viewModel : RecentActivityViewModel by viewModels()
    private val recentAdapter by lazy { RecentFarmerActivityAdapter() }

    override fun getLayoutRes(): Int = R.layout.fragment_recent_activity

    override fun getTitle(): String = "Recent Activity"

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
            this.view?.findNavController()?.navigate(R.id.farmerDetailsFragment, bundle)
        }

        setObserver()
        viewModel.getRecentFarmers()
    }

    private fun setObserver() {
        viewModel.response.observe(viewLifecycleOwner) {
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