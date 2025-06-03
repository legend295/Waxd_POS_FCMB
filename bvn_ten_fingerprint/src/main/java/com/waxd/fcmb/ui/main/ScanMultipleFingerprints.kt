package com.waxd.fcmb.ui.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.scanner.activity.FingerprintScanner
import com.scanner.utils.builder.ThemeOptions
import com.scanner.utils.constants.ScannerConstants
import com.scanner.utils.enums.ScanningType
import com.waxd.fcmb.R
import com.waxd.fcmb.databinding.ActivityScanMultipleFingerprintsBinding
import com.waxd.fcmb.enums.FingerPrints
import com.waxd.fcmb.ui.main.adapter.MainAdapter
import com.waxd.fcmb.utils.FileUtil
import com.waxd.fcmb.utils.serializable
import org.json.JSONObject
import java.io.File
import java.io.IOException

class ScanMultipleFingerprints : AppCompatActivity() {

    private var binding: ActivityScanMultipleFingerprintsBinding? = null
    private val viewModel: MainViewModel by viewModels()
    private val adapter by lazy { MainAdapter(viewModel.list) }
    private var fingerPrintType: FingerPrints? = null

    private val themeOptions = ThemeOptions().apply {
        buttonColor = R.color.pear
        buttonTextColor = R.color.forestGreen
        messageColor = R.color.forestGreen
        titleTextColor = R.color.black
        contentTextColor = R.color.black
        buttonBackground = R.drawable.bg_round_corner_8
        popUpBackground = R.drawable.bg_round_corner_8
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_scan_multiple_fingerprints)
        binding?.lifecycleOwner = this


        binding?.rvFingerprints?.adapter = adapter
        adapter.clickHandler = {
            fingerPrintType = it.fingerPrintType
            startScanning()
//            openGallery()
        }

        binding?.ivBack?.setOnClickListener {
            finish()
        }

        binding?.tvContinue?.setOnClickListener {
            AlertDialog.Builder(it.context).setMessage("BVN Registration Completed")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun openGallery() {
        val mime = arrayOf("image/*")
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mime)
        intent.action = Intent.ACTION_GET_CONTENT
        galleryImageLauncher.launch(intent)
    }

    private var galleryImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                try {
                    val url = data?.data
                    url?.let { uri ->
                        val file = FileUtil.uriToFile(this, uri)
                        viewModel.list[0].imagePath = file?.path
                        adapter.notifyDataSetChanged()
                    }


                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
            }
        }

    private fun startScanning() {

        FingerprintScanner.Builder(this)
            .skipFirebaseActions(true)
            .setScanningType(ScanningType.REGISTRATION)
            .storagePath("biometrics/")
            .setThemeOptions(themeOptions)
            .setCustomData(JSONObject())
//            .newRelicToken("AA8225aa19532b95f2ef0006820193d3b69f45ec47-NRMA")
            .skipLocation(skipLocation = true)
            .start(this, scanningLauncher)
    }

    private val scanningLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val list: ArrayList<File>? = it.data?.serializable(ScannerConstants.DATA)
                val templateList: ArrayList<File>? =
                    it.data?.serializable(ScannerConstants.TEMPLATE_DATA)
                handleResponse(list, templateList)
            }
        }

    private fun handleResponse(list: ArrayList<File>?, templateList: ArrayList<File>?) {
        Log.d("DEBUG", "List: $list")
        try {
            list?.forEachIndexed { index, file ->
                val path = file.path.split(".")[0] + file.path.split(".")[1].replace("wsq", ".jpg")
                Log.d(
                    "DEBUG",
                    "Image Path: $path At index Path $index with Finger print type ${fingerPrintType?.name ?: "null"}"
                )
                when (fingerPrintType) {
                    FingerPrints.THUMB_LEFT, FingerPrints.THUMB_RIGHT -> {
                        viewModel.list[index].imagePath = path
                    }

                    FingerPrints.INDEX_LEFT, FingerPrints.INDEX_RIGHT -> {
                        val pos = index + 2
                        viewModel.list[pos].imagePath = path
                    }

                    FingerPrints.MIDDLE_LEFT, FingerPrints.MIDDLE_RIGHT -> {
                        val pos = index + 4
                        viewModel.list[pos].imagePath = path
                    }

                    FingerPrints.RING_LEFT, FingerPrints.RING_RIGHT -> {
                        val pos = index + 6
                        viewModel.list[pos].imagePath = path
                    }

                    FingerPrints.LITTLE_LEFT, FingerPrints.LITTLE_RIGHT -> {
                        val pos = index + 8
                        viewModel.list[pos].imagePath = path
                    }

                    null -> {}
                }
                adapter.notifyDataSetChanged()
                handleContinueButtonUI()

            }
//            templateList?.forEachIndexed { index, file ->
////                val path = file.path.split(".")[0] + file.path.split(".")[1].replace("wsq", ".jpg")
//                viewModel.list[index].title = file.path
//            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun handleContinueButtonUI() {
        val isAllCaptured = checkIfAllFingerprintsCaptured()
        binding?.tvContinue?.isEnabled = isAllCaptured
        binding?.tvContinue?.alpha = if (isAllCaptured) 1f else 0.5f
    }

    fun checkIfAllFingerprintsCaptured(): Boolean {
        return viewModel.list.all { !it.imagePath.isNullOrEmpty() }
    }
}