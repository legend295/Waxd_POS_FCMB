package com.waxd.pos.fcmb.ui.main.fragments.dashboard

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.scanner.activity.FingerprintScanner
import com.scanner.utils.constants.ScannerConstants
import com.scanner.utils.enums.ScanningType
import com.waxd.fcmb.ui.registration.BVNRegistrationActivity
import com.waxd.pos.fcmb.BuildConfig
//import com.waxd.fcmb.ui.registration.BVNRegistrationActivity
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.app.FcmbApp.Companion.themeOptions
import com.waxd.pos.fcmb.base.BaseFragment
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.databinding.FragmentAgentDashboardBinding
import com.waxd.pos.fcmb.ui.main.fragments.dashboard.adapter.RecentFarmerActivityAdapter
import com.waxd.pos.fcmb.utils.Util.isInternetAvailable
import com.waxd.pos.fcmb.utils.Util.visible
import com.waxd.pos.fcmb.utils.constants.Constants
import com.waxd.pos.fcmb.utils.serializable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import kotlin.collections.forEach

@AndroidEntryPoint
class AgentDashboardFragment : BaseFragment<FragmentAgentDashboardBinding>() {

    private val viewModel: AgentDashboardViewModel by viewModels()
    private val recentAdapter by lazy { RecentFarmerActivityAdapter() }

    override fun getTitle(): String = ""

    override fun getLayoutRes(): Int = R.layout.fragment_agent_dashboard

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isAdded) {
            init()
        }
    }

    override fun init() {
        binding.rvRecentActivity.adapter = recentAdapter
        recentAdapter.clickListener = {
            val bundle = Bundle().apply {
                putString(Constants.IntentKeys.FARMER_ID, it.farmerData?.id)
            }
            this.view?.findNavController()
                ?.navigate(R.id.farmerDetailsFragment, bundle, getNavOptions())
        }
        setObserver()
        if (context?.isInternetAvailable(showMessage = true) == true) {
            viewModel.getRecentFarmers()
        }

        binding.viewBgAddFarmer.setOnClickListener {
            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                val editText1 = EditText(context).apply {
                    hint = "Type unique ID here"
                    maxLines = 1
                    inputType = InputType.TYPE_CLASS_TEXT
                    imeOptions = EditorInfo.IME_ACTION_NEXT
                    filters = arrayOf(android.text.InputFilter.LengthFilter(20))
                }
                val editText2 = EditText(context).apply {
                    hint = "Type phone number here"
                    maxLines = 1
                    inputType = InputType.TYPE_CLASS_PHONE
                    imeOptions = EditorInfo.IME_ACTION_DONE
                    filters = arrayOf(android.text.InputFilter.LengthFilter(10))
                }
                setPadding(50, 40, 50, 10)
                addView(editText1)
                addView(editText2)
            }
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Enter Values")
                .setView(layout)
                .setPositiveButton("OK") { dialog, _ ->
                    val uniqueId = (layout.getChildAt(0) as EditText).text.toString()
                    val phoneNumber = (layout.getChildAt(1) as EditText).text.toString()
                    startScanning(uniqueId, phoneNumber) // or pass both values as needed
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                .show()
            /* this.view?.findNavController()?.navigate(
                 R.id.createLoanApplicationFragment,
                 Bundle(),
                 getNavOptions()
             )*/
        }

        binding.viewBgCreateLoan.setOnClickListener {
            this.view?.findNavController()?.navigate(
                R.id.loanApplicationsFragment,
                Bundle(),
                getNavOptions()
            )
        }

        binding.viewBgListOfFarmers.setOnClickListener {
            this.view?.findNavController()?.navigate(
                R.id.farmersListFragment,
                Bundle(),
                getNavOptions()
            )
        }

        binding.viewBgSearchUser.setOnClickListener {
            this.view?.findNavController()?.navigate(
                R.id.addFarmerFragment,
                Bundle(),
                getNavOptions()
            )
        }

        binding.tvRecentActivity.setOnClickListener {
            this.view?.findNavController()?.navigate(
                R.id.recentActivityFragment,
                Bundle(),
                getNavOptions()
            )
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            if (context?.isInternetAvailable(showMessage = true) == true) {
                viewModel.getRecentFarmers()
            }
        }


    }

    private fun setObserver() {
        viewModel.response.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = false
            binding.progressBarRecentActivity.visible(it is DataResult.Loading)
            when (it) {
                is DataResult.Failure -> {}
                DataResult.Loading -> {}
                is DataResult.Success -> {
                    recentAdapter.setList(it.data)
                }
            }
        }
    }

    private fun startScanning(uniqueId: String, phoneNumber: String) {
        FingerprintScanner.Builder(requireContext())
            .setUniqueId(uniqueId)
            .setPhoneNumber(phoneNumber)
            .setScanningType(ScanningType.REGISTRATION)
            .storagePath("biometrics/")
            .skipFirebaseActions(skipFirebaseActions = true)
            .setKey(BuildConfig.ENCRYPTION_KEY)
            .setThemeOptions(themeOptions)
            .setCustomData(JSONObject())
            .newRelicToken(BuildConfig.NEW_RELIC_TOKEN)
            .skipLocation(skipLocation = false)
            .start(this, scanningLauncher)
    }

    private val scanningLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val list: ArrayList<File>? = it.data?.serializable(ScannerConstants.DATA)
                val templateFileList: ArrayList<File>? = it.data?.serializable(ScannerConstants.TEMPLATE_DATA)
                uploadTemplateFile(templateFileList)
            }
        }

    private fun uploadTemplateFile(templateFileList: ArrayList<File>?) {
        templateFileList?.let { files ->
            if (files.isNotEmpty()) {
                files.forEach { file ->
                    viewModel.enqueue("",file, "application/octet-stream")
                }

            }
        }
    }
}