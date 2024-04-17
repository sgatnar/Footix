package com.example.footixappbachelorarbeit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.footixappbachelorarbeit.R

class CalendarAdapter(private val items: List<CalendarItem>) : RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val descriptionTextView: TextView = view.findViewById(R.id.calendar_item_description)
        val valueTextView: TextView = view.findViewById(R.id.calendar_item_value)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.calendar_listitem, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.descriptionTextView.text = item.description
        holder.valueTextView.text = item.value
        holder.progressBar.max = item.maxProgress
        holder.progressBar.progress = item.progress!!
    }

    override fun getItemCount(): Int {
        return items.size
    }

    data class CalendarItem(val description: String, val value: String, val progress: Int?, val maxProgress: Int)
}
