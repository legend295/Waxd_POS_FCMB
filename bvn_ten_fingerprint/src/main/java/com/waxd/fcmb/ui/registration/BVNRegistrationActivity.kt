package com.waxd.fcmb.ui.registration

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.waxd.fcmb.R
import com.waxd.fcmb.databinding.ActivityBvnregistrationBinding
import com.waxd.fcmb.models.NigerianState
import com.waxd.fcmb.ui.main.ScanMultipleFingerprints
import com.waxd.fcmb.utils.Utils.loadJsonArrayFromRaw

class BVNRegistrationActivity : AppCompatActivity() {

    private val viewModel: BVNRegistrationViewModel by viewModels()
    lateinit var binding: ActivityBvnregistrationBinding
    private var nigerianState: NigerianState? = null
    private var stateMap: MutableMap<String, ArrayList<String?>?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bvnregistration)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        nigerianState = this.loadJsonArrayFromRaw(R.raw.nigerian_states)
        stateMap = nigerianState?.let { viewModel.getNigerianStateMap(it) }

        binding.tvMaritalStatus.setOnClickListener {
            val popupMenu = PopupMenu(this, binding.tvMaritalStatus)
            popupMenu.menu.add(0, 0, 0, "Single")
            popupMenu.menu.add(0, 1, 1, "Married")
            popupMenu.menu.add(0, 2, 2, "Widow")
            popupMenu.menu.add(0, 3, 3, "Widower")
            popupMenu.menu.add(0, 4, 4, "Divorced")
            popupMenu.menu.add(0, 5, 5, "Separated")
            popupMenu.show()
        }

        binding.tvGender.setOnClickListener {
            val popupMenu = PopupMenu(this, binding.tvGender)
            popupMenu.menu.add(0, 0, 0, "Male")
            popupMenu.menu.add(0, 1, 1, "Female")
            popupMenu.show()
        }

        binding.rgSpecialNeeds.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbYes -> {
                    viewModel.request.value?.specialNeeds = true
                }

                R.id.rbNo -> {
                    viewModel.request.value?.specialNeeds = false
                }
            }
            viewModel.request.value = viewModel.request.value
        }

        binding.spinnerStateOfOrigin.onItemSelectedListener = onItemSelectedListener

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.tvCaptureFarmCoordinates.setOnClickListener {
            startActivity(Intent(this, ScanMultipleFingerprints::class.java))
        }
    }

    private val onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            val selectedState = p0?.getItemAtPosition(p2) as String

            viewModel.request.value?.stateOfOrigin = selectedState
            viewModel.request.value = viewModel.request.value

            val state = stateMap?.get(selectedState)
            state?.let {
                val adapter = ArrayAdapter(
                    this@BVNRegistrationActivity,
                    android.R.layout.simple_spinner_item,
                    it
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerLgaOfOrigin.adapter = adapter
            }
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {

        }
    }
}