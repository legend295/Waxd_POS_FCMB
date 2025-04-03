package com.waxd.pos.fcmb.ui.main.fragments.profile.update

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.View
import androidx.navigation.findNavController
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.base.BaseFragment
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.databinding.FragmentUpdateProfileBinding
import com.waxd.pos.fcmb.rest.NotValidException
import com.waxd.pos.fcmb.utils.Util.visible
import com.waxd.pos.fcmb.utils.handlers.ViewClickHandler
import com.waxd.pos.fcmb.utils.showGenderSelectionSheet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpdateProfileFragment : BaseFragment<FragmentUpdateProfileBinding>(), ViewClickHandler {

    private val viewModel: UpdateProfileViewModel by viewModels()

    override fun getLayoutRes(): Int = R.layout.fragment_update_profile

    override fun getTitle(): String  = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAdded && this.view != null) init()
    }

    override fun init() {
        viewModel.setProfileData(viewLifecycleOwner)

        binding.viewModel = viewModel
        binding.viewClickHandler = this

        setObserver()
    }

    private fun setObserver() {
        viewModel.response.observe(viewLifecycleOwner) {
            when (it) {
                is DataResult.Failure -> {
                    updateUI(isLoading = false)
                }
                DataResult.Loading -> {
                    updateUI(isLoading = true)
                }
                is DataResult.Success -> {
                    updateUI(isLoading = false)
                    showToast("Profile Updated")
                    this.view?.findNavController()?.popBackStack()
                }
            }
        }
    }

    private fun updateUI(isLoading: Boolean) {
        binding.progressBar.visible(isLoading)
        binding.btnSubmit.isEnabled = !isLoading
        binding.btnSubmit.alpha = if (isLoading) .5f else 1f
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnSubmit -> {
                try {
                    viewModel.request.value?.isValid()
                    viewModel.updateUser()
                } catch (e: NotValidException) {
                    showToast(e.message ?: "All fields is required.")
                }
            }

            R.id.tvGender -> {
                context?.showGenderSelectionSheet {
                    viewModel.request.value?.gender = it
                    viewModel.request.value = viewModel.request.value
                }
            }
        }
    }


}