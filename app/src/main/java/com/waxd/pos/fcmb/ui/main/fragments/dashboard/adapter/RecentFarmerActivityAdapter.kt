package com.waxd.pos.fcmb.ui.main.fragments.dashboard.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.waxd.pos.fcmb.databinding.LayoutRecentFarmerActivityItemBinding
import com.waxd.pos.fcmb.rest.FarmerResponse
import com.waxd.pos.fcmb.utils.Util.covertTimeToText
import com.waxd.pos.fcmb.utils.Util.loadFarmerImage

class RecentFarmerActivityAdapter : RecyclerView.Adapter<RecentFarmerActivityAdapter.ViewHolder>() {

    private val list = ArrayList<FarmerResponse>()

    lateinit var clickListener: (FarmerResponse) -> Unit

    inner class ViewHolder(private val binding: LayoutRecentFarmerActivityItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(farmerResponse: FarmerResponse) {
            binding.tvTitle.text =
                if (farmerResponse.farmerData?.dateCreated == farmerResponse.farmerData?.dateUpdated) "Farmer Added" else "Farmer Updated"
            binding.tvTime.text = farmerResponse.farmerData?.dateUpdated?.covertTimeToText()
            binding.ivFarmer.loadFarmerImage(farmerResponse.farmerData?.profileImage)

            binding.root.setOnClickListener {
                clickListener(farmerResponse)
            }
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

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setList(list: ArrayList<FarmerResponse>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }
}