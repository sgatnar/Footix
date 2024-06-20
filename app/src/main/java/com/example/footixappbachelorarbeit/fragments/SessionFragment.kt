package com.example.footixappbachelorarbeit

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.footixappbachelorarbeit.ttn.MQTTClient
import com.example.footixappbachelorarbeit.viewModelLiveData.FieldCalculationUtil
import com.example.footixappbachelorarbeit.viewModelLiveData.FootballFieldDrawingView
import com.example.footixappbachelorarbeit.viewModelLiveData.Session
import com.example.footixappbachelorarbeit.viewModelLiveData.SessionDatabase
import com.example.footixappbachelorarbeit.viewModelLiveData.ViewModelFragmentHandler
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.json.JSONObject
import org.osgeo.proj4j.ProjCoordinate
import java.time.LocalDate
import java.time.format.DateTimeFormatter


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

    private lateinit var backButton: ImageView
    private lateinit var greenContainerSettings: ConstraintLayout
    private lateinit var totalDistanceDescription: TextView
    private lateinit var distance: TextView
    private lateinit var infoButton: ImageView
    private lateinit var timeIcon: ImageView
    private lateinit var timerText: TextView
    private lateinit var newCounter: TextView
    private lateinit var dataReceivedLight: Button

    var totalDistance: Double = 0.0
    var previousLat: Double? = null
    var previousLong: Double? = null

    var maxSpeed: Double = 0.0

    var cornersScanned: Int = 0

    private var currentTimeInSeconds: Long = 0
    private var lastTimeWhenCalcMaxSpeed: Long = 0
    private lateinit var alertDialog: AlertDialog
    private lateinit var footballFieldDrawingView: FootballFieldDrawingView
    private lateinit var coroutineTimer: Job

    private var cornerLatitudes = ArrayList<Double>(4)
    private var cornerLongitudes = ArrayList<Double>(4)

    private lateinit var playerCoordinates: ProjCoordinate
    private fun setCorner(lat: Double, long: Double) {
        cornerLatitudes.add(lat)
        cornerLongitudes.add(long)
    }

    private fun calculateCurrentPosition(
        currentLat: Double, currentLong: Double, width: Int, height: Int
    ): ProjCoordinate {
        val initialCoords: ArrayList<ProjCoordinate> = ArrayList()

        for (i in cornerLongitudes.indices) {
            initialCoords.add(
                FieldCalculationUtil.transformCoordinates(
                    cornerLongitudes[i], cornerLatitudes[i]
                )
            )
        }

        val angle: Double = FieldCalculationUtil.angleBetweenPoints(
            initialCoords[0], initialCoords[1]
        ) + Math.toRadians(180.0)

        val rotatedCoords: ArrayList<ProjCoordinate> = ArrayList();
        for (coord: ProjCoordinate in initialCoords) {
            rotatedCoords.add(FieldCalculationUtil.rotatePoint(coord.x, coord.y, -angle))
        }

        val minMaxValues: FieldCalculationUtil.Companion.MinMaxValues =
            FieldCalculationUtil.getMinMaxValues(rotatedCoords)

        val scale: Double =
            FieldCalculationUtil.getScaling(rotatedCoords, minMaxValues, width, height)
        rotatedCoords.stream().forEach { c: ProjCoordinate ->
            c.setValue(
                (c.x - minMaxValues.minLong) * scale, (c.y - minMaxValues.minLat) * scale
            )
        }

        var projCoordinate = FieldCalculationUtil.transformCoordinates(currentLong, currentLat)
        projCoordinate =
            FieldCalculationUtil.rotatePoint(projCoordinate.x, projCoordinate.y, -angle)
        projCoordinate.setValue(
            (projCoordinate.x - minMaxValues.minLong) * scale,
            (projCoordinate.y - minMaxValues.minLat) * scale
        )
        return projCoordinate
    }

    private fun addCornersDialog() {
        val dialogView = layoutInflater.inflate(R.layout.standard_popup_layout_3, null)

        val alertDialogBuilder =
            AlertDialog.Builder(requireContext()).setView(dialogView).setCancelable(false)

        val popupTitleSession = dialogView.findViewById<TextView>(R.id.popupTitle)
        popupTitleSession.text = "Corners scanned"

        val popupDescriptionTextSession = dialogView.findViewById<TextView>(R.id.popupText)
        popupDescriptionTextSession.text = "${cornersScanned}"


        val addCornerButton = dialogView.findViewById<Button>(R.id.cancelButton)
        addCornerButton.text = "ADD"

        addCornerButton.setOnClickListener { dialog ->
            closePopup()

            if (cornersScanned < 3) {
                if (previousLat == null || previousLong == null) {
                    val errorToast = Toast.makeText(
                        requireContext(), "Sorry, try again in a few seconds", Toast.LENGTH_SHORT
                    )

                    errorToast.show()

                } else {
                    setCorner(previousLat!!, previousLong!!)

                    cornersScanned++
                }

                addCornersDialog()

            } else {
                setCorner(previousLat!!, previousLong!!)

                cornersScanned++

                playerCoordinates = calculateCurrentPosition(
                    previousLat!!,
                    previousLong!!,
                    footballFieldDrawingView.width,
                    footballFieldDrawingView.height
                )

                footballFieldDrawingView.playerPosition = playerCoordinates


                showStartSessionPopup()
            }
        }

        alertDialog = alertDialogBuilder.create()

        alertDialog.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
        appDB = SessionDatabase.getDatabase(requireContext())
        viewModel = ViewModelProvider(requireActivity()).get(ViewModelFragmentHandler::class.java)

        if (viewModel.activeMQTTConnection.value == false) {
            connectToMQTT()
        }

        addCornersDialog()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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
        dataReceivedLight = view.findViewById(R.id.data_received_light)

        dataReceivedLight.isEnabled = false
        dataReceivedLight.isClickable = false
        dataReceivedLight.setBackgroundColor(Color.GRAY)

        dialogInformation()

        timeIcon.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.standard_popup_layout_4, null)

            alertDialog =
                AlertDialog.Builder(requireContext()).setView(dialogView).setCancelable(false)
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

            endSessionButton.setOnClickListener {
                closePopup()

                coroutineTimer.cancel()

                viewModel.sessionTimerValue.value = 0
                navigateBottomNavBar(R.id.home)

                CoroutineScope(Dispatchers.Main).launch {
                    delay(3000L)
                    viewModel.activeSession.value = false
                    mqttClient.disconnect()
                }

                writeSessionDataToDB()
            }
            alertDialog.show()
        }

        backButton.setOnClickListener {
            navigateBottomNavBar(R.id.home)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        footballFieldDrawingView = view.findViewById(R.id.footballFieldDrawingView)

        viewModel.activeSession.observe(viewLifecycleOwner) { isActive ->
            if (isActive) {
                timeIcon.visibility = View.VISIBLE
                timerText.visibility = View.VISIBLE

                startSessionTimer()
            } else {
                timeIcon.visibility = View.INVISIBLE
                timerText.visibility = View.INVISIBLE
            }
        }
    }

    fun writeSessionDataToDB() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val date =
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")).toString()
                val distanceStringWithPeriod = "%.2f".format(totalDistance).replace(",", ".")
                val distance = distanceStringWithPeriod.toDouble() //                val speed = 23f
                var time = currentTimeInSeconds
                var timeFormatted = formatTime(time)

                var isAvailable = false

                val existingSessions = appDB.sessionDao().getAllSessions()
                for (session in existingSessions) {
                    if (session.currentDate == date) {
                        isAvailable = true
                        session.totalDistance = distance
                        session.maxSpeed = maxSpeed.toFloat()
                        session.runTime = timeFormatted
                        appDB.sessionDao().update(session)
                        break
                    }
                }

                if (isAvailable) {
                    println("Session updated")
                } else {
                    val session = Session(null, date, distance, maxSpeed.toFloat(), timeFormatted)
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
                Log.e(
                    "writeSessionDataToDB",
                    "Error writing session data to database: ${e.message}",
                    e
                )
            }
        }

        Toast.makeText(requireContext(), R.string.succesfullSession, Toast.LENGTH_SHORT).show()
    }

    private fun connectToMQTT() {
        mqttClient = MQTTClient(requireContext())
        mqttClient.connect(object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.e("MQTT", "Connection successful...${viewModel.activeMQTTConnection.value}")
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
        }, object : MQTTClient.DataListener {

            // Send MQTT back to TTN. If message received correct payload = 1, if not payload = 0
            override fun onDataReceived(long: Double, lat: Double) {
                mqttClient.publish(
                    TOPIC_UPLINK,
                    JSONObject("""{"downlinks":[{"f_port": 1,"frm_payload": "AQ==","priority": "NORMAL"}]}""")
                )

                if (long != null && lat != null) {
                    requireActivity().runOnUiThread {
                        val formattedDistance = calculateDistance(long, lat)
                        val currentSpeed = 22.0
                        Log.e("SPEED", "current max speed: $currentSpeed")

                        if (currentSpeed > maxSpeed) {
                            maxSpeed = currentSpeed
                        }
                        Log.e("SPEED", "absolute max speed: $maxSpeed") //                        }

                        totalDistance += formattedDistance
                        Log.e("Distance", "Distance in meters: $formattedDistance m")
                        Log.e("Total Distance", "Distance in meters: $totalDistance m")
                        val formattedTotalDistance = String.format("%.2f", totalDistance)
                        distance.text = "$formattedTotalDistance km"
                        dataReceivedLight.setBackgroundColor(Color.GREEN)

                        if (cornersScanned >= 4) {
                            playerCoordinates = calculateCurrentPosition(
                                previousLat!!,
                                previousLong!!,
                                footballFieldDrawingView.width,
                                footballFieldDrawingView.height
                            )

                            footballFieldDrawingView.playerPosition = playerCoordinates

                            footballFieldDrawingView.invalidate()
                        }

                    }


                } else { // If not received, send base64 coded 0 --> MA==
                    mqttClient.publish(
                        TOPIC_UPLINK,
                        JSONObject("""{"downlinks":[{"f_port": 1,"frm_payload": "MA==","priority": "NORMAL"}]}""")
                    )
                    Log.e(
                        "Calculation Distance Error", "Calculation of distance is not possible"
                    )

                    requireActivity().runOnUiThread {
                        dataReceivedLight.setBackgroundColor(Color.RED)

                    }
                }

                CoroutineScope(Dispatchers.Main).launch {
                    delay(1000)
                    dataReceivedLight.setBackgroundColor(Color.GRAY)
                }
            }
        })
    }

    fun calculateDistance(currentLong: Double, currentLat: Double): Double {

        val earthRadius = 6371.0

        if (previousLat == null || previousLong == null || previousLong == 0.00 || previousLat == 0.0) {
            previousLat = currentLat
            previousLong = currentLong
            distance.text = "0.00 km"
            return 0.00
        }

        val deltaLat = Math.toRadians(currentLat - previousLat!!)
        val deltaLon = Math.toRadians(currentLong - previousLong!!)

        val a =
            Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) + Math.cos(Math.toRadians(previousLat!!)) * Math.cos(
                Math.toRadians(currentLat)
            ) * Math.sin(
                deltaLon / 2
            ) * Math.sin(deltaLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        previousLat = currentLat
        previousLong = currentLong

        return earthRadius * c

    }

    fun calculateSpeed(distance: Double): Double {
        val currentMaxSpeed = distance / (currentTimeInSeconds - lastTimeWhenCalcMaxSpeed)

        lastTimeWhenCalcMaxSpeed = currentTimeInSeconds

        return currentMaxSpeed
    }

    @SuppressLint("MissingInflatedId")
    private fun dialogInformation() {

        infoButton.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.standard_popup_layout_3, null)

            alertDialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

            val popupTitleSession = dialogView.findViewById<TextView>(R.id.popupTitle)
            popupTitleSession.text = getString(R.string.sessionInfo)

            val popupDescriptionTextSession = dialogView.findViewById<TextView>(R.id.popupText)
            var descriptionFusion = "${getString(R.string.startingDate)}\n${
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")).toString()
            }"
            popupDescriptionTextSession.text = descriptionFusion

            val retryInternetConnectionButton = dialogView.findViewById<Button>(R.id.cancelButton)
            retryInternetConnectionButton.text = getString(R.string.close)
            retryInternetConnectionButton.setOnClickListener {
                closePopup()
            }

            alertDialog.show()
        }
    }

    private fun showStartSessionPopup() {
        val dialogView = layoutInflater.inflate(R.layout.standard_popup_layout_4, null)

        alertDialog =
            AlertDialog.Builder(requireContext()).setView(dialogView).setCancelable(false).create()

        val popupTitleSession = dialogView.findViewById<TextView>(R.id.popupTitle)
        popupTitleSession.text = getString(R.string.startingNewSession)

        val popupDescriptionTextSession = dialogView.findViewById<TextView>(R.id.popupText)
        popupDescriptionTextSession.text = getString(R.string.startSesssionDescriptionText)

        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        cancelButton.text = getString(R.string.cancel)
        cancelButton.setOnClickListener {
            closePopup()

            navigateBottomNavBar(R.id.home)
        }
        val startSessionButton = dialogView.findViewById<Button>(R.id.confirmButton)
        startSessionButton.text = getString(R.string.startSession)
        startSessionButton.setOnClickListener {
            closePopup()
            Log.d("CORNERS", "Lat: $cornerLatitudes, Long: $cornerLongitudes")
            viewModel.activeSession.value = true

            footballFieldDrawingView.invalidate()
        }

        alertDialog.show()
    }

    private fun navigateBottomNavBar(id: Int) {
        val bottomNavBar: BottomNavigationView =
            requireActivity().findViewById(R.id.bottomNavigationView)
        bottomNavBar.selectedItemId = id
    }

    private fun startSessionTimer() {
        currentTimeInSeconds = viewModel.sessionTimerValue.value ?: 0L

        requireActivity().runOnUiThread {
            coroutineTimer = CoroutineScope(Dispatchers.Main).launch {
                while (isActive) {
                    delay(1000L)
                    currentTimeInSeconds++
                    timerText.text = formatTime(currentTimeInSeconds)
                    viewModel.sessionTimerValue.value = currentTimeInSeconds
                    Log.e("Timer Cock", timerText.text.toString())
                }
            }
        }
    }

    private fun formatTime(seconds: Long): String {
        val hours = seconds / 3600
        val remainder = seconds % 3600
        val minutes = remainder / 60
        val secs = remainder % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    private fun closePopup() {
        if (::alertDialog.isInitialized && alertDialog.isShowing) {
            alertDialog.dismiss()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) = SessionFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }
}