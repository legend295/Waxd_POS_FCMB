package com.waxd.fcmb.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.waxd.fcmb.R
import com.waxd.fcmb.databinding.LayoutFingerprintItemsBinding
import com.waxd.fcmb.models.FingerprintData

class MainAdapter(private val list: ArrayList<FingerprintData>) :
    RecyclerView.Adapter<MainAdapter.ViewHolder>() {

    lateinit var clickHandler: (FingerprintData) -> Unit

    inner class ViewHolder(private val binding: LayoutFingerprintItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(fingerprintData: FingerprintData) {
            binding.tvTitle.text = fingerprintData.title
            Glide.with(binding.ivFingerprint).load(fingerprintData.imageUri)
                .placeholder(R.drawable.ic_fingerprint_placeholder)
                .into(binding.ivFingerprint)

            binding.root.setOnClickListener {
                clickHandler(fingerprintData)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutFingerprintItemsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }
}