package com.example.footixappbachelorarbeit

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

class SettingsListViewAdapter(context: Context, private val items: List<String>) :
    ArrayAdapter<String>(context, R.layout.settings_listitem, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.settings_listitem, parent, false)
        }

        val currentItem = items[position]
        //view.itemTitle.text = currentItem
        //view.itemDescription.text = "Description for $currentItem"

        return view!!
    }
}
