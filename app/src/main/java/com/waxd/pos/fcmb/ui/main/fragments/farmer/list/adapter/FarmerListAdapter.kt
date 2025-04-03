package com.waxd.pos.fcmb.ui.main.fragments.farmer.list.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.waxd.pos.fcmb.databinding.LayoutListOfFarmerItemBinding
import com.waxd.pos.fcmb.rest.FarmerData
import com.waxd.pos.fcmb.rest.FarmerResponse
import com.waxd.pos.fcmb.utils.Util.loadFarmerImage

class FarmerListAdapter : RecyclerView.Adapter<FarmerListAdapter.ViewHolder>() {

    private val list = ArrayList<FarmerResponse?>()
    lateinit var clickHandler: (FarmerResponse) -> Unit

    inner class ViewHolder(private val binding: LayoutListOfFarmerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(response: FarmerResponse) {
            binding.data = response.farmerData
            response.farmerData?.profileImage?.let { image -> binding.ivFarmer.loadFarmerImage(image) }
            binding.root.setOnClickListener {
                clickHandler(response)
            }
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

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        list[position]?.let { holder.bind(it) }
    }

    fun add(farmerData: FarmerResponse?) {
        list.add(farmerData)
        notifyItemInserted(list.size - 1)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addAll(list: ArrayList<FarmerResponse>) {
        this.list.clear()
        notifyDataSetChanged()
        list.forEach {
            add(it)
        }
    }

    fun getList() = list

    fun addLoader() {
        add(null)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        list.clear()
        notifyDataSetChanged()
    }

    fun removeLoader() {
        if (list.isNotEmpty() && list[list.size - 1] == null) {
            list.removeAt(list.size - 1)
            notifyItemRemoved(list.size)
        }
    }
}