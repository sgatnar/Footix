package com.example.footixappbachelorarbeit.viewModelLiveData

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewModelFragmentHandler: ViewModel() {

    var amountOfSession = MutableLiveData<Int>(0)
    val sessionTimerValue = MutableLiveData<Long>(0L)
    val currentLat = MutableLiveData<Double>(0.0)
    val currentLong = MutableLiveData<Double>(0.0)

    val activeSession: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().apply {
            value = false
        }
    }

    val activeMQTTConnection: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().apply {
            value = false
        }
    }
}