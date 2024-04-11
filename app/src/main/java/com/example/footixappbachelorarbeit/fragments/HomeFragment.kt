package com.example.footixappbachelorarbeit

import android.app.Dialog
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.footixappbachelorarbeit.adapters.RankingAdapter
import com.example.footixappbachelorarbeit.adapters.RankingItem
import com.example.footixappbachelorarbeit.viewModelLiveData.ViewModelFragmentHandler
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment(){

    private lateinit var view: View
    private lateinit var greenContainerLeft: ConstraintLayout
    private lateinit var greenContainerRight: ConstraintLayout
    private lateinit var greyContainer: ConstraintLayout
    private lateinit var greenContainer: ConstraintLayout
    private lateinit var syncButton: ImageButton
    private lateinit var progressBar: ProgressBar
    lateinit var viewModel: ViewModelFragmentHandler

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
        viewModel = ViewModelProvider(requireActivity()).get(ViewModelFragmentHandler::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        view = inflater.inflate(R.layout.fragment_home, container, false)
        syncButton = view.findViewById(R.id.syncButton)
        progressBar = view.findViewById(R.id.progressBarMQTTConncetion)
        greenContainerLeft = view.findViewById(R.id.greenContainerLeft)
        greenContainerRight = view.findViewById(R.id.greenContainerRight)
        greyContainer = view.findViewById(R.id.greyContainer)
        greenContainer = view.findViewById(R.id.greenContainer)

        hideProgressBar()

        progressBar.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black_footix))
        progressBar.indeterminateTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black_footix))

        startSessionOrHighscore(greenContainerLeft, SessionFragment())
        startSessionOrHighscore(greenContainerRight, SettingsFragment())

        return view
    }

    private fun HomeFragment.startSessionOrHighscore(greenContainer: ConstraintLayout, fragment: Fragment) {
        greenContainer.setOnClickListener {
            if (greenContainer == greenContainerLeft) {
                if (viewModel.activeSession.value == true){
                    val snackbar = Snackbar.make(
                        view.findViewById(R.id.fragment_home), // Replace with your layout ID
                        resources.getString(R.string.activeSession),
                        Snackbar.LENGTH_SHORT
                    ).setBackgroundTint(resources.getColor(R.color.grey_background_footix, null)).setTextColor(resources.getColor(R.color.black_footix)) // Optional: Set success color

                    snackbar.show()

                } else{
                    showProgressBar()
                    progressBar = ProgressBar(requireContext())
                    requireView().findViewById<ViewGroup>(R.id.fragment_session)?.addView(progressBar)

                    Log.d("HomeFragment", "Active session value: ${viewModel.activeSession.value}")

                    Handler(Looper.getMainLooper()).postDelayed({
                        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
                        fragmentTransaction.replace(R.id.frame_layout, fragment)
                        fragmentTransaction.commit()
                    }, 2000)
                }
            } else if(greenContainer == greenContainerRight){
                showHighScorePopup()
            } else
             {
                val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.frame_layout, fragment)
                fragmentTransaction.commit()

                 val snackbar = Snackbar.make(
                     view.findViewById(R.id.fragment_home), // Replace with your layout ID
                     resources.getString(R.string.toastRestartApp),
                     Snackbar.LENGTH_SHORT
                 ).setBackgroundTint(resources.getColor(R.color.grey_background_footix, null)).setTextColor(resources.getColor(R.color.black_footix)) // Optional: Set success color

                 snackbar.show()
            }
        }
    }

    private fun showHighScorePopup() {
        val fragmentManager = childFragmentManager
        val customPopupDialogFragment = CustomPopupDialogFragment()
        customPopupDialogFragment.show(fragmentManager, "CustomPopupDialogFragment")
    }

    fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        progressBar.visibility = View.GONE
    }

    class CustomPopupDialogFragment : DialogFragment() {

        private val rankingList = listOf(
            RankingItem(1, "10 km", "01-04-2024"),
            RankingItem(2, "8 km", "02-04-2024"),
            RankingItem(3, "6 km", "03-04-2024"),
            // Add more items as needed
        )

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.highscore_ranking_popup_layout, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            popupHighscoreCreate(view)
        }

        private fun popupHighscoreCreate(view: View) {
            val popupTitle = view.findViewById<TextView>(R.id.popupTitle)
            val closeButton = view.findViewById<Button>(R.id.cancelButton)
            val rankingListView = view.findViewById<ListView>(R.id.rankingListView)

            popupTitle.text = getString(R.string.highscore)

            val adapter = RankingAdapter(requireContext(), rankingList)
            rankingListView.adapter = adapter

            closeButton.setOnClickListener {
                dismiss()
            }
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return super.onCreateDialog(savedInstanceState).apply {
                setCanceledOnTouchOutside(false)
            }
        }
    }
}



