package com.waxd.pos.fcmb.ui.main.fragments.notification

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.base.BaseFragment
import com.waxd.pos.fcmb.databinding.FragmentNotificationBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationFragment : BaseFragment<FragmentNotificationBinding>() {

    private val viewModel: NotificationViewModel by viewModels()

    override fun getTitle(): String = "Notifications"

    override fun getLayoutRes(): Int = R.layout.fragment_notification

    override fun init() {

    }

}