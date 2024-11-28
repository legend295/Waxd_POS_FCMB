package com.waxd.pos.fcmb.ui.main.fragments.add.farmer

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.navigation.findNavController
import com.scanner.activity.FingerprintScanner
import com.scanner.utils.builder.ThemeOptions
import com.scanner.utils.constants.ScannerConstants
import com.scanner.utils.enums.ScanningType
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.base.BaseFragment
import com.waxd.pos.fcmb.databinding.FragmentAddFarmerBinding
import com.waxd.pos.fcmb.utils.constants.Constants
import com.waxd.pos.fcmb.utils.serializable
import java.io.File
import java.util.Calendar
import java.util.Locale

class AddFarmerFragment : BaseFragment<FragmentAddFarmerBinding>() {

    private var encryptionKey = "com.waxd.pos.24e2c72b-6506-490d-a818-4112526db233"

    override fun getLayoutRes(): Int = R.layout.fragment_add_farmer

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAdded) {
            init()
        }
    }

    override fun init() {
        binding.tvDob.setOnClickListener {
            openDatePicker()
        }

        binding.tvCreateFarmerProfile.setOnClickListener {
            this.view?.findNavController()?.navigate(R.id.successFragment)
        }
        val themeOptions = ThemeOptions().apply {
            buttonColor = R.color.pear
            buttonTextColor = R.color.forestGreen
            messageColor = R.color.forestGreen
            popUpBackground = R.drawable.bg_white_round_6
            buttonBackground = R.drawable.bg_white_round_6
        }
        binding.tvCaptureFingerprint.setOnClickListener {
            FingerprintScanner.Builder(requireContext())
                .setBvnNumber("12345678907")
                .setPhoneNumber("12345678907")
                .setScanningType(ScanningType.REGISTRATION)
                .setKey(encryptionKey)
                .setThemeOptions(themeOptions)
                .start(this, scanningLauncher)
        }

        binding.tvCreateFarmerProfile.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setMessage("Are you sure you want to submit the details.")
                .setPositiveButton("Confirm") { _, _ ->
                    val bundle = Bundle().apply {
                        putString(
                            Constants.MESSAGE_INTENT,
                            "Farmer profile is created successfully."
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

    private fun openDatePicker() {
        val datePicker = DatePickerDialog(requireContext())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -18)
        datePicker.datePicker.maxDate = calendar.timeInMillis
        datePicker.setOnDateSetListener { _, year, month, dayOfMonth ->
            binding.tvDob.text =
                StringBuilder().append(String.format(Locale.getDefault(), "%02d", dayOfMonth))
                    .append("/")
                    .append(month + 1).append("/")
                    .append(year)
        }
        datePicker.show()
    }

    private val scanningLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val list: ArrayList<File>? = it.data?.serializable(ScannerConstants.DATA)
                val isVerified: Boolean? =
                    it.data?.getBooleanExtra(ScannerConstants.VERIFICATION_RESULT, false)
                Log.d(AddFarmerFragment::class.simpleName, list?.size.toString())
                handleResponse(list, isVerified)
            }
        }

    private fun handleResponse(list: ArrayList<File>?, isVerified: Boolean?) {
        if (list.isNullOrEmpty()) {
//                StringBuilder().append("Fingerprint verification :- ").append(isVerified)
            return
        }
        var message = ""
        list.forEach {
            message += "\n${it.path}"
        }
        if (message.isNotEmpty()) {
            showToast("Fingerprint Captured.")
//            tvStatus?.text = StringBuilder().append("File saved to paths :- ").append(message)
        }
    }
}