package com.example.footixappbachelorarbeit.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.footixappbachelorarbeit.R

class RankingAdapter(context: Context, private val rankingList: List<RankingItem>) :
    ArrayAdapter<RankingItem>(context, 0, rankingList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var listItemView = convertView

        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.highscore_items, parent, false)
        }

        val currentItem = rankingList[position]

        val rankingNumberTextView = listItemView?.findViewById<TextView>(R.id.rankingNumberTextView)
        val distanceTextView = listItemView?.findViewById<TextView>(R.id.distanceTextView)
        val dateTextView = listItemView?.findViewById<TextView>(R.id.dateTextView)

        // Set text color here
        rankingNumberTextView?.setTextColor(ContextCompat.getColor(context, R.color.black_footix))
        distanceTextView?.setTextColor(ContextCompat.getColor(context, R.color.black_footix))
        dateTextView?.setTextColor(ContextCompat.getColor(context, R.color.black_footix))

        rankingNumberTextView?.text = currentItem.rankingNumber.toString()
        distanceTextView?.text = currentItem.distance
        dateTextView?.text = currentItem.date

        return listItemView!!
    }
}


data class RankingItem(val rankingNumber: Int, val distance: String, val date: String)