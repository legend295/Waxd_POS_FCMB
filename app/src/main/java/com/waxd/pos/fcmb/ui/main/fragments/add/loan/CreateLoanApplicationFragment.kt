package com.waxd.pos.fcmb.ui.main.fragments.add.loan

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.navigation.findNavController
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.base.BaseFragment
import com.waxd.pos.fcmb.databinding.FragmentCreateLoanApplicationBinding
import com.waxd.pos.fcmb.utils.constants.Constants

class CreateLoanApplicationFragment : BaseFragment<FragmentCreateLoanApplicationBinding>() {

    override fun getLayoutRes(): Int = R.layout.fragment_create_loan_application

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAdded) init()
    }

    override fun init() {
        binding.tvSubmitApplication.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setMessage("Are you sure you want to submit the details.")
                .setPositiveButton("Confirm") { _, _ ->
                    val bundle = Bundle().apply {
                        putString(
                            Constants.MESSAGE_INTENT,
                            "Loan application is submitted successfully.\nLoan Application No - FCMB121"
                        )
                    }
                    this.view?.findNavController()?.navigate(R.id.successFragment, bundle)
                }.setNegativeButton("Cancel", null).show()
        }

        binding.tvCancel.setOnClickListener {
            AlertDialog.Builder(requireContext()).setMessage("Are you sure you want to cancel?")
                .setPositiveButton("Confirm") { _, _ ->
                    this.view?.findNavController()?.popBackStack()
                }.setNegativeButton("Cancel", null).show()
        }
    }
}