package com.example.footixappbachelorarbeit

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

class SettingsListViewAdapter(
    private val context: Context,
    private val items: MutableList<String>,
    private val icons: List<Int>
) : ArrayAdapter<String>(context, R.layout.settings_listitem, items) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val viewHolder: ViewHolder

        if (view == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.settings_listitem, parent, false)
            viewHolder = ViewHolder(
                view.findViewById(R.id.itemIconSuffix),
                view.findViewById(R.id.itemIconPrefix),
                view.findViewById(R.id.itemText)
            )
            view.tag = viewHolder
        } else {
            viewHolder = view.tag as ViewHolder
        }

        val currentItem = items[position]

        val savedText = sharedPreferences.getString("item_$position", currentItem) ?: currentItem
        viewHolder.itemText.text = savedText

        viewHolder.itemIconSuffix.setImageResource(icons[position])

        viewHolder.itemIconPrefix.setOnClickListener {
            showEditTextDialog(position, viewHolder.itemText)
        }

        return view!!
    }

    private data class ViewHolder(
        val itemIconPrefix: ImageView,
        val itemIconSuffix: ImageView,
        val itemText: TextView
    )

    private fun showEditTextDialog(position: Int, textView: TextView) {
        val editText = EditText(context).apply {
            setText(items[position])
            filters = arrayOf<InputFilter>(InputFilter.LengthFilter(15))
            setTextColor(ContextCompat.getColor(context, R.color.black_footix))
            textSize = 18f
            setPadding(32, 16, 0, 16)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                backgroundTintList = ContextCompat.getColorStateList(context, R.color.grey_toolbar_footix)
            } else {
                @Suppress("DEPRECATION")
                setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.grey_toolbar_footix))
            }

        }

        val titleTextView = TextView(context).apply {
            text = "Edit ${items[position]}"
            setTextColor(ContextCompat.getColor(context, R.color.black_footix))
            textSize = 26f
            setPadding(32, 16, 0, 16)
        }

        val dialog = AlertDialog.Builder(context)
            .setCustomTitle(titleTextView) // Set custom title
            .setView(editText)
            .setPositiveButton(context.getString(R.string.save)) { _, _ ->
                val inputText = editText.text.toString().trim()
                items[position] = inputText
                textView.text = inputText

                sharedPreferences.edit().putString("item_$position", inputText).apply()

                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(editText.windowToken, 0)
            }
            .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()

                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(editText.windowToken, 0)
            }
            .setOnDismissListener {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(editText.windowToken, 0)
            }
            .create()

        // Set background color
        dialog.window?.setBackgroundDrawableResource(R.color.grey_background_footix)

        // Set text color
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(context, R.color.black_footix))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(context, R.color.black_footix))
            dialog.findViewById<TextView>(android.R.id.message)?.setTextColor(ContextCompat.getColor(context, R.color.black_footix))
            dialog.findViewById<TextView>(context.resources.getIdentifier("alertTitle", "id", "android"))?.setTextColor(ContextCompat.getColor(context, R.color.black_footix))
        }

        // Show soft keyboard
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

        dialog.show()
    }
}
