package com.example.gomimi.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gomimi.databinding.ManualListRowBinding
import com.example.gomimi.dataClass.ManualInfo

class ManualAdapter(
    private var manualList: List<ManualInfo>,
    private val onItemClicked: (ManualInfo) -> Unit
) : RecyclerView.Adapter<ManualAdapter.ManualViewHolder>() {

    class ManualViewHolder(val binding: ManualListRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManualViewHolder {
        val binding = ManualListRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ManualViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ManualViewHolder, position: Int) {
        val manual = manualList[position]
        holder.binding.manualItemTextView.text = manual.name
        holder.itemView.setOnClickListener {
            onItemClicked(manual)
        }
//        holder.binding.itemButton.setOnClickListener {
//            onItemClicked(manual)
//        }
    }

    override fun getItemCount() = manualList.size

    fun updateData(newManualList: List<ManualInfo>) {
        manualList = newManualList
        notifyDataSetChanged()
    }
}
