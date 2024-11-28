package com.waxd.pos.fcmb.ui.main.fragments.farmer.list

import android.os.Bundle
import android.view.View
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.base.BaseFragment
import com.waxd.pos.fcmb.databinding.FragmentFarmersListBinding
import com.waxd.pos.fcmb.ui.main.fragments.farmer.list.adapter.FarmerListAdapter

class FarmersListFragment : BaseFragment<FragmentFarmersListBinding>() {

    private val adapter by lazy { FarmerListAdapter() }

    override fun getLayoutRes(): Int = R.layout.fragment_farmers_list

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAdded) {
           init()
        }
    }

    override fun init() {
        binding.rvFarmerList.adapter = adapter
    }
}