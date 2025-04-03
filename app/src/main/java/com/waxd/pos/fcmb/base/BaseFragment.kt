package com.waxd.pos.fcmb.base

import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.waxd.pos.fcmb.ui.initial.InitialActivity
import com.waxd.pos.fcmb.ui.main.MainActivity
import com.waxd.pos.fcmb.utils.handlers.LocationPermissionHandler

abstract class BaseFragment<DB : ViewDataBinding> : Fragment(), BaseHandler {
    open lateinit var binding: DB

    @LayoutRes
    abstract fun getLayoutRes(): Int
    abstract fun getTitle(): String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, getLayoutRes(), container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        setTitle(getTitle())
        return binding.root
    }

    open fun getBaseActivity(): BaseActivity? {
        val activity = activity
        if (activity is BaseActivity) {
            return activity
        }
        return null
    }

    abstract fun init()

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }

    override fun showToast(msg: String, length: Int) {
        if (isAdded)
            Toast.makeText(requireContext(), msg, length).show()
    }

    override fun isLocationPermissionGranted(handler: LocationPermissionHandler) {
        when (activity) {
            is MainActivity -> {
                (activity as MainActivity?)?.isLocationPermissionGranted(handler)
            }

            is InitialActivity -> {
                (activity as InitialActivity?)?.isLocationPermissionGranted(handler)
            }
        }
    }

    override fun isLocationEnabled(): Boolean {
        return when (activity) {
            is MainActivity -> {
                (activity as MainActivity?)?.isLocationEnabled() ?: false
            }

            is InitialActivity -> {
                (activity as InitialActivity?)?.isLocationEnabled() ?: false
            }

            else -> false
        }
    }

    private fun setTitle(title: String) {
        if (activity is MainActivity) {
            (activity as MainActivity?)?.setTitle(title)
        }
    }

    override fun checkStoragePermission(): Boolean {
        return getBaseActivity()?.checkStoragePermission() ?: false
    }

    override fun getStoragePermissionArray(): Array<String> {
        return getBaseActivity()?.getStoragePermissionArray() ?: arrayOf()
    }
}