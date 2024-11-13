package com.waxd.pos.fcmb.ui.main.fragments.dashboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.waxd.pos.fcmb.databinding.LayoutRecentFarmerActivityItemBinding

class RecentFarmerActivityAdapter : RecyclerView.Adapter<RecentFarmerActivityAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: LayoutRecentFarmerActivityItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutRecentFarmerActivityItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = 5

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }
}