package com.example.footixappbachelorarbeit

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.footixappbachelorarbeit.viewModelLiveData.ViewModelFragmentHandler
import com.google.android.material.bottomnavigation.BottomNavigationView

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SettingsFragment : Fragment() {

    lateinit var viewModel: ViewModelFragmentHandler
    private lateinit var view: View
    private lateinit var toolbar: Toolbar
    private lateinit var textToolbar: TextView
    private lateinit var nameDescription: TextView
    private lateinit var sessionText: TextView
    private lateinit var amountSession: TextView
    private lateinit var backButton: ImageView
    private val items: MutableList<String> = mutableListOf("Name", "Birthday", "Position", "Height", "Weight")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.fragment_settings, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(ViewModelFragmentHandler::class.java)

        initViews()
        setupListView()
        setupAppBar()
        setupBackButton()

        viewModel.amountOfSession.observe(this) { value ->
            amountSession.text = viewModel.amountOfSession.value.toString()
        }

        return view
    }

    private fun initViews() {
        toolbar = view.findViewById(R.id.includedAppBarSettings)
        textToolbar = view.findViewById(R.id.textOfIncludedAppBarSession)
        backButton = view.findViewById(R.id.backButton)
        nameDescription = view.findViewById(R.id.nameDescriptionGreenContainerSettings)
        sessionText = view.findViewById(R.id.sessionTextGreenContainerSettings)
        amountSession = view.findViewById(R.id.amountSessionCounterGreenContainer)
    }

    private fun setupListView() {
        val listView: ListView = view.findViewById(R.id.settingsListView)
        val iconList = listOf(
            R.drawable.baseline_person_settings,
            R.drawable.baseline_calendar,
            R.drawable.baseline_location,
            R.drawable.baseline_calendar,
            R.drawable.baseline_height,
            R.drawable.baseline_weight
        )

        val adapter = SettingsListViewAdapter(requireContext(), items, iconList)
        listView.adapter = adapter
    }

    private fun setupAppBar() {
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        nameDescription.text = getString(R.string.developerName)
        sessionText.text = getString(R.string.session)
    }

    private fun setupBackButton() {
        backButton.setOnClickListener {
            val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.frame_layout, HomeFragment())
            fragmentTransaction.commit()
        }
    }

    private fun BottomNavigationView.changeColor(@ColorRes defaultColor: Int, @ColorRes selectedColor: Int) {
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_pressed),
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(
                ContextCompat.getColor(context, defaultColor),
                ContextCompat.getColor(context, selectedColor),
                ContextCompat.getColor(context, defaultColor)
            )
        )
        itemIconTintList = colorStateList
        itemTextColor = colorStateList
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
