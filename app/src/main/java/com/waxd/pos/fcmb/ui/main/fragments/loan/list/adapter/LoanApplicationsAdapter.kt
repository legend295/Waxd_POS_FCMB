package com.waxd.pos.fcmb.ui.main.fragments.loan.list.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.waxd.pos.fcmb.databinding.LayoutLoanApplicationsItemBinding
import com.waxd.pos.fcmb.databinding.LayoutProgressBarItemBinding
import com.waxd.pos.fcmb.rest.FarmerLoanApplicationResponse
import com.waxd.pos.fcmb.utils.ProgressBarViewHolder

class LoanApplicationsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_PROGRESS = 1
    }

    lateinit var clickHandler: (FarmerLoanApplicationResponse) -> Unit

    private val list: ArrayList<FarmerLoanApplicationResponse?> = ArrayList()

    inner class ViewHolder(private val binding: LayoutLoanApplicationsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(loanApplicationData: FarmerLoanApplicationResponse) {
            loanApplicationData.loanData?.let {
                binding.data = it

                binding.root.setOnClickListener {
                    clickHandler(loanApplicationData)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            VIEW_TYPE_ITEM -> {
                val binding = LayoutLoanApplicationsItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return ViewHolder(binding)
            }

            else -> {
                val binding = LayoutProgressBarItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return ProgressBarViewHolder(binding)
            }
        }
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_ITEM) {
            list[position]?.let { (holder as ViewHolder).bind(it) }
        }
    }

    override fun getItemViewType(position: Int): Int = if (list[position] == null) {
        VIEW_TYPE_PROGRESS
    } else {
        VIEW_TYPE_ITEM
    }

    fun add(data: FarmerLoanApplicationResponse?) {
        list.add(data)
        notifyItemInserted(list.size - 1)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addAll(list: ArrayList<FarmerLoanApplicationResponse>) {
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