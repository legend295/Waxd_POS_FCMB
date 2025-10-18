package com.waxd.pos.fcmb.ui.main.fragments.search

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.base.BaseFragment
import com.waxd.pos.fcmb.databinding.FragmentSearchBinding
import com.waxd.pos.fcmb.utils.Util.visible
import com.waxd.pos.fcmb.utils.animateVisibility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File

@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentSearchBinding>() {

    private val viewModel: SearchViewModel by viewModels()
    val file by lazy { File(context?.filesDir?.absolutePath + "/12345678907") }
    override fun getLayoutRes(): Int = R.layout.fragment_search
    override fun getTitle(): String = "Search"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (this.view != null && isAdded) {
            init()
        }
    }

    override fun init() {

        val assetFiles = listOf(
            requireContext().assets.open("2024-11-29-15-21-040-ISO-Template.bin"),
            requireContext().assets.open("2024-11-29-15-21-041-ISO-Template.bin")
        )
        if (!file.exists()) {
            file.mkdirs()
            assetFiles.forEachIndexed { index, inputStream ->
                val outFile = File(file, "assetFile_$index.bin")
                outFile.outputStream().use { output ->
                    inputStream.copyTo(output)
                }
                inputStream.close()
            }
        }

        /* binding.tvBvnRegistration.setOnClickListener {
             startActivity(Intent(requireContext(), BVNRegistrationActivity::class.java))
         }*/

        //

        binding.tvAddOne.setOnClickListener {
            addFilesTimes(1)
        }

        binding.tvAddTen.setOnClickListener {
            addFilesTimes(10)
        }


        binding.btnSyncNow.setOnClickListener {
            viewModel.dispatchNow()
        }

        lifecycleScope.launch {
            viewModel.filesCount.collect { counts ->
                binding.tvTotalCount.text = counts.totalCount.toString()
                binding.tvPendingCount.text = counts.pendingCount.toString()
                binding.tvFailedCount.text = counts.failedCount.toString()
                binding.tvSuccessCount.text = counts.successCount.toString()
                binding.tvInProgressCount.text = counts.inProgressCount.toString()

                binding.btnSyncNow.animateVisibility(isVisible = counts.failedCount > 0)
            }
        }
    }

    private fun addFilesTimes(n: Int) {
        val file = File(context?.filesDir?.absolutePath + "/12345678907")
        val totalFiles = ArrayList<File>()
        for (i in 1..n) {
            val saveToFolder = getFile(getRandomNumber())
            if (file.isDirectory) {
                val files = file.listFiles()
                files?.forEach { f ->
                    if (f.isFile) {
                        val newFile = File(saveToFolder, f.name)
                        f.copyTo(newFile, overwrite = true)
                        totalFiles.add(newFile)
                    }
                }
            }
        }
        totalFiles.forEach { newFile ->
            viewModel.enqueue(newFile, "application/octet-stream")
        }
    }

    fun getRandomNumber(): String {
        val min = 10000000000
        val max = 99999999999
        return (min..max).random().toString()
    }

    fun getFile(fileName: String): File {
        val folder = File(context?.filesDir?.absolutePath + "/$fileName")
        if (folder.exists()) {
            getFile(getRandomNumber())
        }
        return folder
    }

}