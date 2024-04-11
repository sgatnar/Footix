package com.example.footixappbachelorarbeit.viewModelLiveData

import java.text.SimpleDateFormat
import java.util.concurrent.atomic.AtomicInteger
import java.util.*

class Session(
    val sessionNumber: Int,
    val currentDate: Date,
    var totalDistance: Float,
    var maxSpeed: Float,
    private var runTime: Long
) {

    companion object {
        private val sessionCounter = AtomicInteger(0)
        fun createNewSession(): Session {
            val currentDate = Date()
            val sessionNumber = sessionCounter.incrementAndGet()
            return Session(
                sessionNumber,
                currentDate,
                0.0f,
                0f,
                0L
            )
        }
    }

    private var timer: Timer? = null

    init {
        startTimer()
    }

    private fun startTimer() {
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runTime += 1000
            }
        }, 0, 1000)
    }

    fun updateDistance(distance: Float) {
        totalDistance += distance
    }

    fun updateMaxSpeed(speed: Float) {
        if (speed > maxSpeed) {
            maxSpeed = speed
        }
    }

    fun getRunTimeInSeconds(): Long {
        return runTime / 1000
    }

    fun getFormattedDate(): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(currentDate)
    }
}

fun main() {
    // Neue Session erstellen
    val session1 = Session.createNewSession()
    println("Das ist das Session Objekt 1")
    println(session1.sessionNumber)
    println(session1.getFormattedDate())
    println(session1.totalDistance)
    session1.updateDistance(1.5f)
    println(session1.totalDistance)
    println(session1.getRunTimeInSeconds())

    var session2 = Session.createNewSession()

    println("Das ist das Session Objekt 1")
    println(session2.sessionNumber)
    println(session2.getFormattedDate())
    println(session2.totalDistance)
    session2.updateDistance(1.5f)
    println(session2.totalDistance)
    println(session2.getRunTimeInSeconds())
}
