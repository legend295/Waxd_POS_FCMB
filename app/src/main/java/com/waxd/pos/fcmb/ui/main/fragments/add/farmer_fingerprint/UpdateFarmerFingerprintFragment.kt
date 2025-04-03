package com.waxd.pos.fcmb.ui.main.fragments.add.farmer_fingerprint

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import com.scanner.activity.FingerprintScanner
import com.scanner.utils.constants.ScannerConstants
import com.scanner.utils.enums.ScanningType
import com.waxd.pos.fcmb.BuildConfig
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.app.FcmbApp.Companion.themeOptions
import com.waxd.pos.fcmb.base.BaseFragment
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.databinding.FragmentUpdateFarmerFingerprintBinding
import com.waxd.pos.fcmb.rest.FarmerData
import com.waxd.pos.fcmb.utils.Util.loadImage
import com.waxd.pos.fcmb.utils.Util.visible
import com.waxd.pos.fcmb.utils.constants.Constants
import com.waxd.pos.fcmb.utils.handlers.ViewClickHandler
import com.waxd.pos.fcmb.utils.serializable
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import java.io.File

@AndroidEntryPoint
class UpdateFarmerFingerprintFragment : BaseFragment<FragmentUpdateFarmerFingerprintBinding>(),
    ViewClickHandler {

    private val viewModel: UpdateFarmerFingerprintViewModel by viewModels()

    override fun getTitle(): String = "Farm Details"

    override fun getLayoutRes(): Int = R.layout.fragment_update_farmer_fingerprint

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAdded) init()
    }

    override fun init() {
        binding.viewClickHandler = this

        val farmerData = arguments?.serializable<FarmerData>(Constants.IntentKeys.DATA)

        farmerData?.let {
            viewModel.farmerData.value = it
            binding.data = it
            viewModel.getUserById()
        }

        setObserver()
    }

    private fun setObserver() {
        viewModel.userResponse.observe(viewLifecycleOwner) {
            when (it) {
                is DataResult.Failure -> {
                    binding.tvCaptureFingerprint.visible(isVisible = true)
                }

                DataResult.Loading -> {}
                is DataResult.Success -> {
                    binding.tvCaptureFingerprint.visible(
                        !(it.data.farmerData?.fingerPrintSyncedOnCloud ?: false)
                    )
                    binding.tvUpdateFingerprint.visible(
                        it.data.farmerData?.fingerPrintSyncedOnCloud ?: false
                    )
                }
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvCaptureFingerprint -> {
                startScanning()
            }
        }
    }

    private fun startScanning() {
        viewModel.farmerData.value?.let {
            it.id?.let { it1 ->
                FingerprintScanner.Builder(requireContext())
                    .setUniqueId(it1)
                    .setPhoneNumber(it.phoneNumber ?: "")
                    .setScanningType(ScanningType.REGISTRATION)
                    .storagePath("biometrics/")
                    .setKey(BuildConfig.ENCRYPTION_KEY)
                    .setThemeOptions(themeOptions)
                    .setCustomData(JSONObject())
                    .newRelicToken(BuildConfig.NEW_RELIC_TOKEN)
                    .skipLocation(skipLocation = false)
                    .start(this, scanningLauncher)
            }
        }
    }

    private val scanningLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val list: ArrayList<File>? = it.data?.serializable(ScannerConstants.DATA)
                handleResponse(list)
            }
        }

    private fun handleResponse(list: ArrayList<File>?) {
        Log.d("DEBUG", "List: $list")
        try {
            list?.forEachIndexed { index, file ->
                val path = file.path.split(".")[0] + file.path.split(".")[1].replace("wsq", ".jpg")
                Log.d("DEBUG", "Path: $path")
                if (index == 0) {
                    binding.ivScannerLeft.loadImage(path)
                } else if (index == 1) {
                    binding.ivScannerRight.loadImage(path)
                }
//                viewModel.getUserById()
                binding.tvCaptureFingerprint.visible(
                    isVisible = false
                )
                binding.tvUpdateFingerprint.visible(
                    true
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}