package com.example.gomimi.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gomimi.R
import com.example.gomimi.dataClass.History
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class HistoryAdapter(private var historyList: List<History>, private val onItemClicked: (History) -> Unit) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemNameTextView: TextView = itemView.findViewById(R.id.textView8)
        val timestampTextView: TextView = itemView.findViewById(R.id.textView9)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_list_row, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = historyList[position]
        // ★ Historyオブジェクトのプロパティをバインド
        holder.itemNameTextView.text = item.name
        holder.timestampTextView.text = formatTimestamp(item.createdAt)

        holder.itemView.setOnClickListener {
            onItemClicked(item)
        }
    }

    override fun getItemCount() = historyList.size

    // ★ データを更新するためのメソッドを追加
    fun updateData(newHistoryList: List<History>) {
        historyList = newHistoryList
        notifyDataSetChanged() // データが変更されたことをアダプターに通知
    }

    // ★ 日付文字列をフォーマットするヘルパー関数
    private fun formatTimestamp(isoString: String): String {
        return try {
            val odt = OffsetDateTime.parse(isoString)
            val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
            odt.format(formatter)
        } catch (e: Exception) {
            isoString // パースに失敗した場合は元の文字列を返す
        }
    }
}
