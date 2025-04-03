package com.waxd.pos.fcmb.ui.initial.fragments.register

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.base.BaseFragment
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.databinding.FragmentRegistrationBinding
import com.waxd.pos.fcmb.datastore.KeyStore
import com.waxd.pos.fcmb.datastore.KeyStore.encryptData
import com.waxd.pos.fcmb.rest.NotValidException
import com.waxd.pos.fcmb.ui.initial.InitialActivity
import com.waxd.pos.fcmb.utils.Util.visible
import com.waxd.pos.fcmb.utils.handlers.ViewClickHandler
import com.waxd.pos.fcmb.utils.showGenderSelectionSheet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegistrationFragment : BaseFragment<FragmentRegistrationBinding>(), ViewClickHandler {

    private val viewModel: RegistrationViewModel by viewModels()

    override fun getTitle(): String =""

    override fun getLayoutRes(): Int = R.layout.fragment_registration

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isAdded) {
            init()
            binding.viewModel = viewModel
            binding.viewClickHandler = this
        }
    }

    override fun init() {
        viewModel.response.observe(viewLifecycleOwner) {
            when (it) {
                is DataResult.Failure -> {
                    it.message?.let { it1 -> showToast(it1) }
                    updateBtnLoginUI(isVisible = false)
                }

                DataResult.Loading -> {
                    updateBtnLoginUI(isVisible = true)
                }

                is DataResult.Success -> {
                    it.data.message?.let { message -> showToast(message) }
                    this.view?.findNavController()?.popBackStack()
                }
            }
        }

        viewModel.isSecondStep.observe(viewLifecycleOwner) {
            binding.groupFirstStep.visible(!it)
            binding.groupSecondStep.visible(it)
        }
    }

    private fun updateBtnLoginUI(isVisible: Boolean) {
        binding.progressBar.visible(isVisible)
        binding.btnSubmit.isEnabled = !isVisible
        binding.btnSubmit.alpha = if (isVisible) 0.5f else 1f
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnSubmit -> {
                if (viewModel.isSecondStep.value == true)
                    try {
                        viewModel.request.value?.isSecondScreenValid()
                        viewModel.createUser()
                    } catch (e: NotValidException) {
                        showToast(e.message ?: "All fields is required.")
                    }
                else {
                    try {
                        viewModel.request.value?.isFirstScreenValid()
                        viewModel.isSecondStep.value = true
                    } catch (e: NotValidException) {
                        showToast(e.message ?: "All fields is required.")
                    }
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

    fun handleBack() {
        if (viewModel.isSecondStep.value == true) {
            viewModel.isSecondStep.value = false
        } else {
            this.view?.findNavController()?.popBackStack()
        }
    }

}