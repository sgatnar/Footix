package com.example.footixappbachelorarbeit

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.footixappbachelorarbeit.adapters.CalendarAdapter
import com.example.footixappbachelorarbeit.adapters.RankingAdapter
import com.example.footixappbachelorarbeit.adapters.RankingItem
import com.example.footixappbachelorarbeit.viewModelLiveData.SessionDatabase
import com.example.footixappbachelorarbeit.viewModelLiveData.ViewModelFragmentHandler
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.harrywhewell.scrolldatepicker.DayScrollDatePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate


class HomeFragment : Fragment(){

    private lateinit var view: View
    private lateinit var appDB: SessionDatabase
    private lateinit var greenContainerLeft: ConstraintLayout
    private lateinit var greenContainerRight: ConstraintLayout
    private lateinit var greyContainer: ConstraintLayout
    private lateinit var greenContainer: ConstraintLayout
    private lateinit var syncButton: ImageButton
    private lateinit var progressBar: ProgressBar
    lateinit var viewModel: ViewModelFragmentHandler
    lateinit var calendar: DayScrollDatePicker
    lateinit var currentDate: LocalDate
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CalendarAdapter
    private lateinit var items: List<CalendarAdapter.CalendarItem>

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
        viewModel = ViewModelProvider(requireActivity()).get(ViewModelFragmentHandler::class.java)
        appDB = SessionDatabase.getDatabase(requireContext())
    }

    @SuppressLint("ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        view = inflater.inflate(R.layout.fragment_home, container, false)
        syncButton = view.findViewById(R.id.syncButton)
        progressBar = view.findViewById(R.id.progressBarMQTTConncetion)
        greenContainerLeft = view.findViewById(R.id.greenContainerLeft)
        greenContainerRight = view.findViewById(R.id.greenContainerRight)
        greyContainer = view.findViewById(R.id.greyContainer)
        greenContainer = view.findViewById(R.id.greenContainer)
        calendar = view.findViewById(R.id.day_date_picker)

        initCalendar()
        //writeDB()

        recyclerView = view.findViewById(R.id.recyclerView)

        items = listOf(
            CalendarAdapter.CalendarItem(getString(R.string.distance), getString(R.string.calenderInitDistance), 0, 100),
            CalendarAdapter.CalendarItem(getString(R.string.maxSpeed), getString(R.string.calendarInitMaxSpeed), 0, 100),
            CalendarAdapter.CalendarItem(getString(R.string.runTime), getString(R.string.calendarInitRunTime), 0, 100)
        )

        adapter = CalendarAdapter(items)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        hideProgressBar()

        progressBar.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.yellow_footix))
        progressBar.indeterminateTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.yellow_footix))

        startSessionOrHighscore(greenContainerLeft, SessionFragment())
        startSessionOrHighscore(greenContainerRight, SettingsFragment())

        syncButton.setOnClickListener{
            reloadFragment()
        }

        return view
    }

    @SuppressLint("SimpleDateFormat")
    private fun initCalendar() {
        currentDate = LocalDate.now()

        val yearStart = currentDate.year
        val monthStart = currentDate.monthValue
        val dayStart = currentDate.dayOfMonth

        calendar.setStartDate(1, 4, 2024)
        calendar.setEndDate(dayStart, monthStart, yearStart)
        calendar.getSelectedDate { date ->
            if (date != null) {
                val formatter = SimpleDateFormat("dd.MM.yyyy")
                val formattedDate = formatter.format(date.time)
                readSessionDataFromDB(formattedDate)
            }
        }
    }

    private fun HomeFragment.startSessionOrHighscore(greenContainer: ConstraintLayout, fragment: Fragment) {
        greenContainer.setOnClickListener {
            if (greenContainer == greenContainerLeft) {
                if (viewModel.activeSession.value == true){
                    Toast.makeText(requireContext(), R.string.activeSession, Toast.LENGTH_SHORT).show()
                } else{

                    val bottomNavBar: BottomNavigationView = requireActivity().findViewById(R.id.bottomNavigationView)
                    bottomNavBar.selectedItemId = R.id.session

                    showProgressBar()
                    progressBar = ProgressBar(requireContext())
                    requireView().findViewById<ViewGroup>(R.id.fragment_session)?.addView(progressBar)

                    Log.d("HomeFragment", "Active session value: ${viewModel.activeSession.value}")

                    /*Handler(Looper.getMainLooper()).postDelayed({
                        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
                        //fragmentTransaction.replace(R.id.frame_layout, fragment)
                        fragmentTransaction.commit()
                    }, 2000)*/
                }
            } else if(greenContainer == greenContainerRight){
                showHighScorePopup()
            } else
             {
                val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.frame_layout, fragment)
                fragmentTransaction.commit()

                 Toast.makeText(requireContext(), R.string.toastRestartApp, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun readSessionDataFromDB(selectedDate: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val sessions = appDB.sessionDao().getAllSessions()

                val sessionForSelectedDate = sessions.find { it.currentDate == selectedDate }

                val items = if (sessionForSelectedDate != null) {
                    val distanceInt = sessionForSelectedDate.totalDistance?.toInt() ?: 0
                    val distance = "${sessionForSelectedDate.totalDistance} km"

                    val speedInt = sessionForSelectedDate.maxSpeed?.toInt() ?: 0
                    val speed = "${sessionForSelectedDate.maxSpeed} km/h"

                    val total = sessionForSelectedDate.runTime
                    val parts = total?.split(":")
                    val hours = parts?.getOrNull(0)?.toInt() ?: 0
                    val minutes = parts?.getOrNull(1)?.toInt() ?: 0
                    val seconds = parts?.getOrNull(2)?.toInt() ?: 0

                    var minProgBar = 0

                    if (hours==1){
                        minProgBar = minutes + 60
                    }else if (hours == 2){
                        minProgBar = 100
                    } else{
                        minProgBar = minutes
                    }

                    // Ensure two-digit format for all time units
                    val formattedHours = String.format("%02d", hours)
                    val formattedMinutes = String.format("%02d", minutes)
                    val formattedSeconds = String.format("%02d", seconds)

                    val time = "$formattedHours:$formattedMinutes:$formattedSeconds h"

                    listOf(
                        CalendarAdapter.CalendarItem(getString(R.string.distance), distance, distanceInt, 20),
                        CalendarAdapter.CalendarItem(getString(R.string.maxSpeed), speed, speedInt, 40),
                        CalendarAdapter.CalendarItem(getString(R.string.runTime), time, minProgBar, 100)
                    )
                } else {
                    Log.e("Error: ", "No session found for the selected date")
                    listOf(
                        CalendarAdapter.CalendarItem(getString(R.string.distance), getString(R.string.calenderInitDistance), 0, 20),
                        CalendarAdapter.CalendarItem(getString(R.string.maxSpeed), getString(R.string.calendarInitMaxSpeed), 0, 40),
                        CalendarAdapter.CalendarItem(getString(R.string.runTime), getString(R.string.calendarInitRunTime), 0, 100)
                    )
                }

                withContext(Dispatchers.Main) {
                    adapter = CalendarAdapter(items)
                    recyclerView.adapter = adapter
                    recyclerView.layoutManager = LinearLayoutManager(requireContext())
                }
            } catch (e: Exception) {
                Log.e("readSessionDataFromDB", "Error reading session data from database: ${e.message}", e)
            }
        }
    }

    suspend fun getAllSessions() {
        withContext(Dispatchers.IO) {
            val allSessions = appDB.sessionDao().getAllSessions()
            allSessions.forEach { session ->
                Log.d("Database Entry", session.toString())
            }
        }
    }

    private fun reloadFragment() {
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, HomeFragment())
        fragmentTransaction.commit()
    }

    private fun showHighScorePopup() {
        GlobalScope.launch(Dispatchers.IO) {
            val allSessions = appDB.sessionDao().getAllSessions()
            val sortedSessions = allSessions.sortedByDescending { it.totalDistance }
            val rankingList = sortedSessions.mapIndexed { index, session ->
                RankingItem(index + 1, "${session.totalDistance} km", session.currentDate)
            }.take(5) // Take only the top three items

            withContext(Dispatchers.Main) {
                val fragmentManager = childFragmentManager
                val customPopupDialogFragment = CustomPopupDialogFragment()
                customPopupDialogFragment.setRankingList(rankingList)
                customPopupDialogFragment.show(fragmentManager, "CustomPopupDialogFragment")
            }
        }
    }

    fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        progressBar.visibility = View.GONE
    }

    class CustomPopupDialogFragment : DialogFragment() {

        private val rankingList: MutableList<RankingItem> = mutableListOf()
        private var rankingAdapter: RankingAdapter? = null

        /*private val rankingList = listOf(
            RankingItem(1, "10 km", "01.04.2024"),
            RankingItem(2, "8 km", "02.04.2024"),
            RankingItem(3, "6 km", "03.04.2024"),
        )*/

        fun setRankingList(list: List<RankingItem>) {
            rankingList.clear()
            rankingList.addAll(list)
        }

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

            popupTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.black_footix))
            closeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black_footix))

            popupTitle.text = getString(R.string.highscore)

            rankingAdapter = RankingAdapter(requireContext(), rankingList)
            rankingListView.adapter = rankingAdapter

            closeButton.setOnClickListener {
                dismiss()
            }
        }

        /*private fun popupHighscoreCreate(view: View) {
            val popupTitle = view.findViewById<TextView>(R.id.popupTitle)
            val closeButton = view.findViewById<Button>(R.id.cancelButton)
            val rankingListView = view.findViewById<ListView>(R.id.rankingListView)

            popupTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.black_footix))
            closeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black_footix))

            popupTitle.text = getString(R.string.highscore)

            rankingAdapter = RankingAdapter(requireContext(), rankingList)
            rankingListView.adapter = rankingAdapter

            closeButton.setOnClickListener {
                dismiss()
            }
        }*/

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return super.onCreateDialog(savedInstanceState).apply {
                setCanceledOnTouchOutside(false)
            }
        }
    }
}