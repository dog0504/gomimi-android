package com.example.test

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.test.databinding.ManualListRowBinding
import com.example.test.retrofit.ManualInfo

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



//import android.content.Context
//import android.content.Intent
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import androidx.recyclerview.widget.RecyclerView
//import com.example.test.ManualDetailActivity
//import com.example.test.ManualItem
//import com.example.test.R
//
//class ManualAdapter(private val context: Context, private val manualList: List<ManualItem>) :
//    RecyclerView.Adapter<ManualAdapter.ManualViewHolder>() {
//
//    class ManualViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val itemButton: Button = view.findViewById(R.id.itemButton)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManualViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.manual_list_row, parent, false)
//        return ManualViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ManualViewHolder, position: Int) {
//        val item = manualList[position]
//        holder.itemButton.text = item.title
//        holder.itemButton.setOnClickListener {
//            val intent = Intent(context, ManualDetailActivity::class.java)
//            intent.putExtra("title", item.title)
//            intent.putExtra("description", item.description)
//            context.startActivity(intent)
//        }
//    }
//
//    override fun getItemCount(): Int = manualList.size
//}
