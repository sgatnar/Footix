package com.example.footixappbachelorarbeit.viewModelLiveData

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewModelFragmentHandler: ViewModel() {

    var amountOfSession = MutableLiveData<Int>(12)

    val sessionTimerValue = MutableLiveData<Long>(0L)

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

    fun refreshData() {
        //amountOfSession.value = amountOfSession.value?.plus(1)
        //sessionTimerValue.value = sessionTimerValue.value?.plus(1000)
    }
}