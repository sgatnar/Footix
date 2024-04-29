package com.example.footixappbachelorarbeit

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.footixappbachelorarbeit.ttn.MQTTClient
import com.example.footixappbachelorarbeit.viewModelLiveData.FootballFieldDrawingView
import com.example.footixappbachelorarbeit.viewModelLiveData.Session
import com.example.footixappbachelorarbeit.viewModelLiveData.SessionDatabase
import com.example.footixappbachelorarbeit.viewModelLiveData.ViewModelFragmentHandler
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Timer
import java.util.TimerTask
import kotlin.properties.Delegates

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SessionFragment : Fragment() {

    // MQTT inits
    private lateinit var mqttClient: MQTTClient
    val TOPIC_UPLINK = "v3/footix-gnss-application@ttn/devices/eui-70b3d57ed0066110/up"
    val TOPIC_DOWNLINK = "v3/footix-gnss-application@ttn/devices/eui-70b3d57ed0066110/down/push"
    var subscribedUplink: Boolean = false
    var subscribedDownlink: Boolean = false

    lateinit var viewModel: ViewModelFragmentHandler
    private lateinit var view: View
    private lateinit var appDB: SessionDatabase
    private var timer: Timer? = null

    private lateinit var backButton: ImageView
    private lateinit var greenContainerSettings: ConstraintLayout
    private lateinit var totalDistanceDescription: TextView
    private lateinit var distance: TextView
    private lateinit var infoButton: ImageView
    private lateinit var timeIcon: ImageView
    private lateinit var timerText: TextView
    var totalDistance: Double = 0.0
    var previousLong: Double? = null
    var previousLat: Double? = null
    private var currentTimeInSeconds by Delegates.notNull<Long>()
    private lateinit var alertDialog: AlertDialog
    private lateinit var footballFieldDrawingView: FootballFieldDrawingView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
        appDB = SessionDatabase.getDatabase(requireContext())
        viewModel = ViewModelProvider(requireActivity()).get(ViewModelFragmentHandler::class.java)

        if (viewModel.activeMQTTConnection.value == false) {
            connectToMQTT()
        }

        viewModel.activeSession.observe(this) { isActive ->
            Log.d("ViewModel", "activeSession value: $isActive")

            if (isActive == false) {
                showPopup()
            } else if (isActive) {
                closePopup()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.fragment_session, container, false)

        backButton = view.findViewById(R.id.backButton)
        greenContainerSettings = view.findViewById(R.id.greenContainerSettings)
        totalDistanceDescription = view.findViewById(R.id.totalDistanceDescription)
        totalDistanceDescription.text = getString(R.string.totalDistanceDescription)
        distance = view.findViewById(R.id.distance)
        distance.text = getString(R.string.initKilometers) + " km"
        infoButton = view.findViewById(R.id.infoIcon)
        timeIcon = view.findViewById(R.id.timeIcon)
        timerText = view.findViewById(R.id.timerText)

        dialogInformation()

        timeIcon.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.standard_popup_layout_4, null)

            alertDialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create()

            val popupTitleSession = dialogView.findViewById<TextView>(R.id.popupTitle)
            popupTitleSession.text = getString(R.string.session)

            val popupDescriptionTextSession = dialogView.findViewById<TextView>(R.id.popupText)
            popupDescriptionTextSession.text = getString(R.string.endSession)

            val cancelSessionButton = dialogView.findViewById<Button>(R.id.cancelButton)
            cancelSessionButton.text = getString(R.string.cancel)
            cancelSessionButton.setOnClickListener {
                alertDialog.dismiss()
            }

            val endSessionButton = dialogView.findViewById<Button>(R.id.confirmButton)
            endSessionButton.text = getString(R.string.stopSession)
            alertDialog.show()

            endSessionButton.setOnClickListener {

                /*CoroutineScope(Dispatchers.IO).launch {
                    appDB.sessionDao().clearSessions()
                }*/

                onDestroy()
                viewModel.sessionTimerValue.value = 0

                val fragmentTransaction =
                    requireActivity().supportFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.frame_layout, HomeFragment())
                fragmentTransaction.commit()

                CoroutineScope(Dispatchers.Main).launch {
                    delay(3000)
                    viewModel.activeSession.value = false
                    mqttClient.disconnect()
                }

                closePopup()
                writeSessionDataToDB()
            }
        }

        backButton.setOnClickListener {
            onDestroy()
            val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.frame_layout, HomeFragment())
            fragmentTransaction.commit()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        footballFieldDrawingView = view.findViewById(R.id.footballFieldDrawingView)

        footballFieldDrawingView.updateField()

        viewModel.activeSession.observe(viewLifecycleOwner) { isActive ->
            if (isActive) {
                timeIcon.visibility = View.VISIBLE
                timerText.visibility = View.VISIBLE

                startSessionTimer()
            } else {
                timeIcon.visibility = View.GONE
                timerText.visibility = View.GONE
            }
        }
    }

    fun writeSessionDataToDB() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")).toString()
                val distance = "%.2f".format(totalDistance).toDouble()
                val speed = 23f
                var time = currentTimeInSeconds
                var timeFormatted = formatTime(time)

                var isAvailable = false

                val existingSessions = appDB.sessionDao().getAllSessions()
                for (session in existingSessions) {
                    if (session.currentDate == date) {
                        isAvailable = true
                        session.totalDistance = distance
                        session.maxSpeed = speed
                        session.runTime = timeFormatted
                        appDB.sessionDao().update(session)
                        break
                    }
                }

                if (isAvailable) {
                    println("Session updated")
                } else {
                    val session = Session(null, date, distance, speed, timeFormatted)
                    appDB.sessionDao().insert(session)
                    println("New session inserted")
                }

                val numSessions = appDB.sessionDao().getCount()
                viewModel.amountOfSession.postValue(numSessions)
                delay(5000)
                Log.d(
                    "SessionDao",
                    "Number of sessions after insertion: ${viewModel.amountOfSession.value}"
                )
            } catch (e: Exception) {
                Log.e("writeSessionDataToDB", "Error writing session data to database: ${e.message}", e)
            }
        }

        val currentView = view
        val currentContext = requireContext()

        val snackbar = Snackbar.make(
            currentView!!,
            currentContext.resources.getString(R.string.succesfullSession),
            Snackbar.LENGTH_SHORT
        ).setBackgroundTint(
            currentContext.getResources().getColor(R.color.grey_background_footix, null)
        )
            .setTextColor(
                currentContext.getResources().getColor(R.color.black_footix)
            )

        snackbar.show()
    }

    private fun connectToMQTT() {
        mqttClient = MQTTClient(requireContext())
        mqttClient.connect(
            object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d("MQTT", "Connection successful...${viewModel.activeMQTTConnection.value}")
                    if (!subscribedUplink) {
                        mqttClient.subscribeToTopic(TOPIC_UPLINK, 0)
                    }
                    if (!subscribedDownlink) {
                        mqttClient.subscribeToTopic(TOPIC_DOWNLINK, 0)
                    }
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e("MQTT", "Connection failure", exception)
                }
            },
            object : MQTTClient.DataListener {

                override fun onDataReceived(long: Double, lat: Double) {

                    mqttClient.publish(
                        TOPIC_DOWNLINK, JSONObject("""{"downlinks":[{"f_port": 1,"frm_payload": "AQ==","priority": "NORMAL"}]}""")
                    )

                    if (long != null && lat != null) {
                        val formattedDistance = calculateDistance(long, lat)
                        totalDistance += formattedDistance
                        val formattedDistanceMeters = String.format("%.2f", formattedDistance)

                        Log.e("Distance", "Distance in meters: $formattedDistance m")
                        Log.e("Total Distance", "Distance in meters: $totalDistance m")
                        val formattedTotalDistance = String.format("%.2f", totalDistance)

                        distance.text = "$formattedTotalDistance km"
                    }else{
                        Log.e("Calculation Distance Error", "Calculation of distance is not possible")
                    }
                }
            }
        )
    }

    fun calculateDistance(currentLong: Double, currentLat: Double): Double {

        val earthRadius = 6371.0

        if (previousLat == null || previousLong == null || previousLong == 0.00 || previousLat == 0.0) {
            previousLat = currentLat
            previousLong = currentLong
            distance.text = "Init phase"
            return 0.0
        }

        val deltaLat = Math.toRadians(currentLat - previousLat!!)
        val deltaLon = Math.toRadians(currentLong - previousLong!!)

        val a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(Math.toRadians(previousLat!!)) * Math.cos(Math.toRadians(currentLat)) *
                Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        previousLat = currentLat
        previousLong = currentLong

        return earthRadius * c
    }

    @SuppressLint("MissingInflatedId")
    private fun dialogInformation() {
        infoButton.setOnClickListener {

            val dialogView = layoutInflater.inflate(R.layout.standard_popup_layout_3, null)

            alertDialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            val popupTitleSession = dialogView.findViewById<TextView>(R.id.popupTitle)
            popupTitleSession.text = getString(R.string.sessionInfo)

            val popupDescriptionTextSession = dialogView.findViewById<TextView>(R.id.popupText)
            popupDescriptionTextSession.text = getString(R.string.startingDate)

            val retryInternetConnectionButton = dialogView.findViewById<Button>(R.id.cancelButton)
            retryInternetConnectionButton.text = getString(R.string.close)
            retryInternetConnectionButton.setOnClickListener {
                closePopup()
            }

            alertDialog.show()
        }
    }

    private fun showPopup() {
        val dialogView = layoutInflater.inflate(R.layout.standard_popup_layout_4, null)

        alertDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val popupTitleSession = dialogView.findViewById<TextView>(R.id.popupTitle)
        popupTitleSession.text = getString(R.string.startingNewSession)

        val popupDescriptionTextSession = dialogView.findViewById<TextView>(R.id.popupText)
        popupDescriptionTextSession.text = getString(R.string.startSesssionDescriptionText)

        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        cancelButton.text = getString(R.string.cancel)
        cancelButton.setOnClickListener {
            val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.frame_layout, HomeFragment())
            fragmentTransaction.commit()
            closePopup()
        }
        val startSessionButton = dialogView.findViewById<Button>(R.id.confirmButton)
        startSessionButton.text = getString(R.string.startSession)
        startSessionButton.setOnClickListener {
            closePopup()
            viewModel.activeSession.value = true
        }

        alertDialog.show()
    }

    private fun startSessionTimer() {
        timer?.cancel()
        timer = Timer()

        currentTimeInSeconds = viewModel.sessionTimerValue.value ?: 0L

        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                activity?.runOnUiThread {
                    currentTimeInSeconds++
                    timerText.text = formatTime(currentTimeInSeconds)
                    viewModel.sessionTimerValue.value = currentTimeInSeconds
                }
            }
        }, 0, 1000)
    }

    private fun formatTime(seconds: Long): String {
        val hours = seconds / 3600
        val remainder = seconds % 3600
        val minutes = remainder / 60
        val secs = remainder % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    fun closePopup() {
        if (::alertDialog.isInitialized && alertDialog.isShowing) {
            alertDialog.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SessionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
