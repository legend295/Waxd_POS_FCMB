package com.waxd.pos.fcmb.ui.main.fragments.loan.details

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.navigation.findNavController
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.base.BaseFragment
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.databinding.FragmentLoanDetailBinding
import com.waxd.pos.fcmb.utils.Util.isInternetAvailable
import com.waxd.pos.fcmb.utils.Util.visible
import com.waxd.pos.fcmb.utils.constants.Constants
import com.waxd.pos.fcmb.utils.handlers.ViewClickHandler
import com.waxd.pos.fcmb.utils.showEditFieldDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoanDetailFragment : BaseFragment<FragmentLoanDetailBinding>(), ViewClickHandler {

    private val viewModel: LoanDetailViewModel by viewModels()

    override fun getLayoutRes(): Int = R.layout.fragment_loan_detail

    override fun getTitle(): String = "Loan Details"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAdded) {
            binding.viewModel = viewModel
            binding.viewClickHandler = this
            init()
        }
    }

    override fun init() {
        viewModel.loanId = arguments?.getString(Constants.IntentKeys.LOAN_ID)
        viewModel.loanId?.let {
            if (context?.isInternetAvailable(showMessage = true) == true) {
                binding.parent.visible(isVisible = true)
                viewModel.getLoanDetailsById(it)
            }
        } ?: run {
            binding.parent.visible(isVisible = false)
            AlertDialog.Builder(requireContext())
                .setTitle("Loan ID not found.")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    this.view?.findNavController()?.popBackStack()
                }
                .show()
        }

        observerLiveData()
    }

    private fun observerLiveData() {
        viewModel.response.observe(viewLifecycleOwner) {
            when (it) {
                is DataResult.Failure -> {}
                DataResult.Loading -> {}
                is DataResult.Success -> {
                    binding.data = it.data
                    binding.group2.visible(isVisible = true)
                }
            }

            binding.progressBarApi.visible(it == DataResult.Loading)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvWithDraw -> {
                context?.let {
                    AlertDialog.Builder(it)
                        .setTitle("Withdraw Loan Amount")
                        .setMessage("The withdrawal of the loan amount has been successfully submitted.")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }

            R.id.tvTransferLoanAmount -> {
                context?.let {
                    it.showEditFieldDialog(
                        message = "Transfer Loan Amount",
                        hint = "Enter Amount",
                        inputType = InputType.TYPE_CLASS_NUMBER
                    ) { value, isOkay ->
                        if (isOkay && value.isNotEmpty())
                            AlertDialog.Builder(it)
                                .setTitle("Transfer Loan Amount")
                                .setMessage("The transfer of the loan amount has been successfully submitted.")
                                .setPositiveButton("OK") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .show()
                    }
                }
            }
        }
    }

}