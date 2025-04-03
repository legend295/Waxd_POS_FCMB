package com.waxd.pos.fcmb.ui.main.fragments.add.farmer

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.scanner.activity.FingerprintScanner
import com.scanner.utils.builder.ThemeOptions
import com.scanner.utils.constants.ScannerConstants
import com.scanner.utils.enums.ScanningType
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.base.BaseFragment
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.databinding.FragmentAddFarmerBinding
import com.waxd.pos.fcmb.rest.FarmerData
import com.waxd.pos.fcmb.rest.NotValidException
import com.waxd.pos.fcmb.ui.main.MainActivity
import com.waxd.pos.fcmb.utils.constants.Constants
import com.waxd.pos.fcmb.utils.handlers.ViewClickHandler
import com.waxd.pos.fcmb.utils.serializable
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.Calendar
import java.util.Locale
import javax.annotation.meta.When

@AndroidEntryPoint
class AddFarmerFragment : BaseFragment<FragmentAddFarmerBinding>(), ViewClickHandler {

    private var encryptionKey = "com.waxd.pos.24e2c72b-6506-490d-a818-4112526db233"
    private val viewModel: AddFarmerViewModel by viewModels()
    private var from: Int? = null
    private var isUpdating = false

    private val themeOptions = ThemeOptions().apply {
        buttonColor = R.color.pear
        buttonTextColor = R.color.forestGreen
        messageColor = R.color.forestGreen
        popUpBackground = R.drawable.bg_white_round_6
        buttonBackground = R.drawable.bg_white_round_6
    }

    override fun getTitle(): String = ""

    override fun getLayoutRes(): Int = R.layout.fragment_add_farmer

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAdded) {
            init()
        }
    }

    override fun init() {
        binding.viewClickHandler = this
        binding.viewModel = viewModel

        from = arguments?.getInt(Constants.FromScreen.FROM)

        // Manage Updating and creating Farmer
        isUpdating = from == Constants.FromScreen.FARMER_DETAILS
        binding.isUpdating = isUpdating
        if (isUpdating) {
            (activity as MainActivity?)?.setTitle("Update Farmer")
            val farmerData = arguments?.serializable<FarmerData>(Constants.IntentKeys.DATA)
            farmerData?.let {
                viewModel.farmerData.value = it
                viewModel.request.value?.setFarmerData(it)
                viewModel.request.value = viewModel.request.value
            }
        }else{
            (activity as MainActivity?)?.setTitle("Add New Farmer")
        }

        setObserver()
    }

    private fun setObserver() {
        viewModel.response.observe(viewLifecycleOwner) {
            when (it) {
                is DataResult.Failure -> {
                    it.message?.let { it1 -> showToast(it1) }
                }

                DataResult.Loading -> {}
                is DataResult.Success -> {
                    this.view?.findNavController()?.popBackStack()
                    val bundle = Bundle().apply {
                        putString(
                            Constants.MESSAGE_INTENT,
                            "Farmer profile is created successfully."
                        )
                    }
                    this.view?.findNavController()?.navigate(R.id.successFragment, bundle)
                }
            }
            handleCreateButtonUI(isInProgress = it == DataResult.Loading)
        }

        viewModel.farmerUpdateResponse.observe(viewLifecycleOwner) {
            when (it) {
                is DataResult.Failure -> {
                    it.message?.let { it1 -> showToast(it1) }
                }

                DataResult.Loading -> {}
                is DataResult.Success -> {
                    this.view?.findNavController()?.popBackStack()
                    val bundle = Bundle().apply {
                        putString(
                            Constants.MESSAGE_INTENT,
                            "Farmer profile updated successfully."
                        )
                    }
                    this.view?.findNavController()?.navigate(R.id.successFragment, bundle)
                }
            }

            handleCreateButtonUI(isInProgress = it == DataResult.Loading)
        }
    }

    private fun handleCreateButtonUI(isInProgress: Boolean) {
        binding.tvCreateFarmerProfile.isEnabled = !isInProgress
        binding.tvCreateFarmerProfile.alpha = if (isInProgress) .5f else 1f
        binding.progressBar.visibility = if (isInProgress) View.VISIBLE else View.GONE
    }

    private fun openDatePicker() {
        val datePicker = DatePickerDialog(requireContext())
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -18)
        datePicker.datePicker.maxDate = calendar.timeInMillis
        datePicker.setOnDateSetListener { _, year, month, dayOfMonth ->
            binding.tvDob.text =
                StringBuilder()
                    .append(year)
                    .append("-")
                    .append(String.format(Locale.getDefault(), "%02d", (month + 1))).append("-")
                    .append(String.format(Locale.getDefault(), "%02d", dayOfMonth))

            viewModel.request.value?.dob = binding.tvDob.text.toString()
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

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvCaptureFarmLocation -> {
                this.view?.findNavController()?.navigate(R.id.captureLocationFragment)
            }

            R.id.tvDob -> {
                openDatePicker()
            }

            R.id.tvCreateFarmerProfile -> {
                AlertDialog.Builder(requireContext())
                    .setMessage("Are you sure you want to submit the details.")
                    .setPositiveButton("Confirm") { _, _ ->
                        try {
                            viewModel.request.value?.isValid()
                            if (isUpdating) {
                                // Update farmer data
                                viewModel.updateFarmer()
                            } else {
                                // register new farmer
                                viewModel.registerFarmer()
                            }
                        } catch (e: NotValidException) {
                            e.message?.let { showToast(it) }
                        }
                    }.setNegativeButton("Cancel", null).show()

            }

            R.id.tvCaptureFingerprint -> {
                FingerprintScanner.Builder(requireContext())
                    .setUniqueId("12345678907")
                    .setPhoneNumber("12345678907")
                    .setScanningType(ScanningType.REGISTRATION)
                    .setKey(encryptionKey)
                    .setThemeOptions(themeOptions)
                    .start(this, scanningLauncher)

            }

            R.id.tvCancel -> {
                AlertDialog.Builder(requireContext()).setMessage("Are you sure you want to cancel?")
                    .setPositiveButton("Confirm") { _, _ ->
                        this.view?.findNavController()?.popBackStack()
                    }.setNegativeButton("Cancel", null).show()
            }
        }
    }
}