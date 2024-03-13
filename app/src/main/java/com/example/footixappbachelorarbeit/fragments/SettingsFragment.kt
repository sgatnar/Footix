package com.example.footixappbachelorarbeit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val listView: ListView = view.findViewById(R.id.settingsListView)

        // Icons for the list
        val iconList = listOf(R.drawable.baseline_person_settings, R.drawable.baseline_calendar, R.drawable.baseline_location, R.drawable.baseline_calendar, R.drawable.baseline_height, R.drawable.baseline_weight)
        val items = listOf("Name", "Birthday", "Position", "Height", "Weight", "App notifications", "Darkmode")

        // Create the adapter and set it to the ListView
        val adapter = SettingsListViewAdapter(requireContext(), items)
        listView.adapter = adapter

        // Find the Toolbar view
        val toolbar: Toolbar = view.findViewById(R.id.includedAppBarSettings)
        val textToolbar: TextView = view.findViewById(R.id.textOfIncludedAppBarSettings)
        val backButton: ImageView = view.findViewById(R.id.backButton)
        // Set the Toolbar as the support action bar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}