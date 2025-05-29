package com.waxd.pos.fcmb.ui.main.fragments.loan.list

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.base.BaseFragment
import com.waxd.pos.fcmb.databinding.FragmentLoanApplicationsBinding
import com.waxd.pos.fcmb.rest.FarmerLoanApplicationResponse
import com.waxd.pos.fcmb.ui.main.fragments.loan.list.adapter.LoanApplicationsAdapter
import com.waxd.pos.fcmb.utils.PaginationScrollListener
import com.waxd.pos.fcmb.utils.Util.isInternetAvailable
import com.waxd.pos.fcmb.utils.Util.visible
import com.waxd.pos.fcmb.utils.constants.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoanApplicationsFragment : BaseFragment<FragmentLoanApplicationsBinding>() {


    private val viewModel: LoanApplicationsViewModel by viewModels()
    private val adapter by lazy { LoanApplicationsAdapter() }
    private var searchQuery = ""

    override fun getLayoutRes(): Int = R.layout.fragment_loan_applications

    override fun getTitle(): String = "Loan Applications"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAdded) {
            init()
        }
    }


    override fun init() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            hitApi()
        }
        setAdapter()
        setObservers()

        hitApi()
    }

    private fun hitApi() {
        if (context?.isInternetAvailable(true) == true) {
            viewModel.getFarmerLoanApplication("", null)
        } else {
            binding.swipeRefreshLayout.isRefreshing = false
            binding.progressBar.visible(isVisible = false)
        }
    }

    private fun setAdapter() {
        context?.let {
            val layoutManager = LinearLayoutManager(it)
            binding.rvLoanApplication.layoutManager = layoutManager
            binding.rvLoanApplication.adapter = adapter

            adapter.clickHandler = {
                val bundle = Bundle().apply {
                    putString(Constants.IntentKeys.LOAN_ID, it.loanData?.id)
                }
                this.view?.findNavController()
                    ?.navigate(R.id.loanDetailFragment, bundle, getNavOptions())
            }

            binding.rvLoanApplication.addOnScrollListener(object :
                PaginationScrollListener(layoutManager) {
                override fun loadMoreItems() {
                    if (it.isInternetAvailable(true)) {
                        viewModel.isLoading = true
                        adapter.addLoader()
                        Handler(Looper.getMainLooper()).postDelayed({
                            loadNextPage()
                        }, 100)
                    }
                }

                override val isLastPage: Boolean
                    get() = viewModel.isLastPage
                override val isLoading: Boolean
                    get() = viewModel.isLoading

            })
        }
    }

    fun loadNextPage() {
        viewModel.getFarmerLoanApplication(
            query = searchQuery,
            lastVisibleDocument = adapter.getList()
                .lastOrNull()?.document, // Start after the last visible document

        )
    }

    private fun setObservers() {
        viewModel.farmerLoanApplicationsResponse.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = false
            updateLoanApplications(it)
        }
    }

    private fun updateLoanApplications(it: ArrayList<FarmerLoanApplicationResponse>) {
        binding.progressBar.visible(isVisible = false)
        adapter.addAll(it)
        binding.tvEmptyMessage.visible(isVisible = adapter.getList().isEmpty())
        viewModel.isLastPage = it.size < 10 || it.isEmpty()
        viewModel.isLoading = false
        adapter.removeLoader()
    }

}