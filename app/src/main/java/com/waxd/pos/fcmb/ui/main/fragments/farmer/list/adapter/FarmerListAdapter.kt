package com.waxd.pos.fcmb.ui.main.fragments.farmer.list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.waxd.pos.fcmb.databinding.LayoutListOfFarmerItemBinding
import com.waxd.pos.fcmb.databinding.LayoutRecentFarmerActivityItemBinding

class FarmerListAdapter : RecyclerView.Adapter<FarmerListAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: LayoutListOfFarmerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutListOfFarmerItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = 10

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }
}