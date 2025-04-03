package com.waxd.pos.fcmb.ui.main.fragments.add.farm_location.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.waxd.pos.fcmb.databinding.LayoutFarmImagesItemBinding
import com.waxd.pos.fcmb.model.FarmImagesData
import com.waxd.pos.fcmb.rest.FarmerData
import com.waxd.pos.fcmb.utils.Util.loadFarmImage
import com.waxd.pos.fcmb.utils.Util.loadImage
import com.waxd.pos.fcmb.utils.Util.visible

class FarmImagesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val list = ArrayList<FarmImagesData>()
    lateinit var emptyClickListener: () -> Unit
    lateinit var itemClickHandler: (Int) -> Unit
    lateinit var deleteClickHandler: (FarmImagesData, Int) -> Unit
    private var farmerId = ""

    object ViewType {
        const val ITEM = 0
        const val EMPTY = 1
    }

    inner class ViewHolder(private val binding: LayoutFarmImagesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: FarmImagesData) {
            binding.ivAdd.visible(isVisible = false)
            binding.ivDelete.visible(isVisible = true)
            if (data.url is String)
                binding.ivFarm.loadFarmImage("$farmerId/${data.url}")
            else binding.ivFarm.loadImage(data.url)

            binding.viewBlur.visible(data.isUploading)
            binding.progressBar.visible(data.isUploading)

            binding.ivDelete.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION)
                    deleteClickHandler(data, adapterPosition)
            }

            binding.ivFarm.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION)
                    itemClickHandler(adapterPosition)
            }
        }
    }

    inner class EmptyViewHolder(private val binding: LayoutFarmImagesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.ivDelete.visible(isVisible = false)
            binding.root.setOnClickListener {
                emptyClickListener()
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ViewType.EMPTY -> {
                val binding =
                    LayoutFarmImagesItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                return EmptyViewHolder(binding)
            }

            else -> {
                val binding =
                    LayoutFarmImagesItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                return ViewHolder(binding)
            }
        }

    }

    override fun getItemCount(): Int = 3

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ViewType.EMPTY -> (holder as EmptyViewHolder).bind()
            else -> (holder as ViewHolder).bind(list[position])
        }
    }

    override fun getItemViewType(position: Int): Int =
        if (list.size <= position) ViewType.EMPTY else ViewType.ITEM

    @SuppressLint("NotifyDataSetChanged")
    fun add(data: FarmImagesData) {
        list.add(data)
        notifyDataSetChanged()
    }

    fun removeAt(position: Int) {
        list.removeAt(position)
        notifyItemRemoved(position)
    }

    fun getList() = list

    fun setFarmerId(id: String) {
        farmerId = id
    }

}