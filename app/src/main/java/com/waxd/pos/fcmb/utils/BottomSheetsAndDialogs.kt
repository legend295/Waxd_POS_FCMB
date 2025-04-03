package com.waxd.pos.fcmb.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.databinding.LayoutGenderDialogBinding
import com.waxd.pos.fcmb.databinding.LayoutImagePickerSheetBinding
import com.waxd.pos.fcmb.utils.handlers.ViewClickHandler


fun Context.showGenderSelectionSheet(callback: (String) -> Unit) {
    val dialog = BottomSheetDialog(this, R.style.DialogStyle)
    val layout = LayoutGenderDialogBinding.inflate(LayoutInflater.from(this), null, false)

    layout.viewClickHandler = object : ViewClickHandler {
        override fun onClick(v: View) {
            when (v.id) {
                R.id.tvMale-> callback("Male")
                R.id.tvFemale-> callback("Female")
                R.id.tvOthers-> callback("Others")
            }
            dialog.dismiss()
        }
    }
    dialog.dismissWithAnimation = true
    dialog.setContentView(layout.root)
    dialog.setCommonSettings()
    dialog.show()
}

fun Context.showImagePickerDialog(callBack: (Boolean) -> Unit) {
    val sheet = BottomSheetDialog(this, R.style.BottomSheetStyle)
    val layout =
        LayoutImagePickerSheetBinding.inflate(LayoutInflater.from(this), null, false)

    layout.clickHandler = object : ViewClickHandler {
        override fun onClick(v: View) {
            when (v.id) {
                R.id.ivClose -> {}

                R.id.tvFromCamera -> {
                    callBack(true)
                }

                R.id.tvFromGallery -> {
                    callBack(false)
                }
            }
            sheet.dismissWithAnimation = true
            sheet.dismiss()
        }
    }

    sheet.setContentView(layout.root)
    sheet.setCommonSettings()
    sheet.show()
}


fun Dialog.setCustomSettings() {
    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    window?.setDimAmount(0.4f)
    window?.statusBarColor = Color.TRANSPARENT
    window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
    )
}

fun BottomSheetDialog.setCommonSettings() {
    behavior.skipCollapsed = true
    behavior.state = BottomSheetBehavior.STATE_EXPANDED

    dismissWithAnimation = true
    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    window?.setDimAmount(0.4f)
    window?.statusBarColor = Color.TRANSPARENT
}