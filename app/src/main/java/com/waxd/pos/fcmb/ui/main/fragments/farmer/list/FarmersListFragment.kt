package com.waxd.pos.fcmb.ui.main.fragments.farmer.list

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.base.BaseFragment
import com.waxd.pos.fcmb.databinding.FragmentFarmersListBinding
import com.waxd.pos.fcmb.rest.FarmerData
import com.waxd.pos.fcmb.rest.FarmerResponse
import com.waxd.pos.fcmb.ui.main.fragments.farmer.list.adapter.FarmerListAdapter
import com.waxd.pos.fcmb.utils.PaginationScrollListener
import com.waxd.pos.fcmb.utils.Util.debouncedTextChanges
import com.waxd.pos.fcmb.utils.Util.isInternetAvailable
import com.waxd.pos.fcmb.utils.Util.visible
import com.waxd.pos.fcmb.utils.constants.Constants
import com.waxd.pos.fcmb.utils.firebase.FirebaseWrapper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FarmersListFragment : BaseFragment<FragmentFarmersListBinding>() {

    private val viewModel: FarmersListViewModel by viewModels()

    @Inject
    lateinit var firebaseWrapper: FirebaseWrapper
    private val adapter by lazy { FarmerListAdapter() }

    override fun getTitle(): String = "List Of Farmers"

    override fun getLayoutRes(): Int = R.layout.fragment_farmers_list

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAdded) {
            init()
        }
    }

    override fun init() {
        setAdapter()

        setObserver()

        if (context?.isInternetAvailable(showMessage = true) == true) {
            binding.progressBar.visible(isVisible = true)
            // Set up the debounced search
            binding.etSearch.debouncedTextChanges(
                debounceDuration = 500, // 500ms debounce
                coroutineScope = lifecycleScope,
                onTextChanged = { query ->
                    viewModel.getFarmers(query, null)
                }
            )
        } else {
            binding.progressBar.visible(isVisible = false)
        }

    }

    private fun setAdapter() {
        context?.let {
            val layoutManager = LinearLayoutManager(it)
            binding.rvFarmerList.layoutManager = layoutManager
            binding.rvFarmerList.adapter = adapter
            adapter.clickHandler = {
                val bundle = Bundle().apply {
                    putString(Constants.IntentKeys.FARMER_ID, it.farmerData?.id)
                }
                this.view?.findNavController()?.navigate(R.id.farmerDetailsFragment, bundle)
            }

            binding.rvFarmerList.addOnScrollListener(object :
                PaginationScrollListener(layoutManager) {
                override fun loadMoreItems() {
                    if (context?.isInternetAvailable() == true) {
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
        viewModel.getFarmers(
            query = "",
            lastVisibleDocument = adapter.getList()
                .lastOrNull()?.document, // Start after the last visible document

        )
    }

    private fun setObserver() {
        viewModel.farmersData.observe(viewLifecycleOwner) {
            updateFarmers(it)
        }
    }

    private fun updateFarmers(it: ArrayList<FarmerResponse>) {
        binding.progressBar.visible(isVisible = false)
        adapter.addAll(it)
        binding.tvEmptyMessage.visible(isVisible = adapter.getList().isEmpty())
        viewModel.isLastPage = it.size < 10
        viewModel.isLoading = false
        adapter.removeLoader()
    }
}