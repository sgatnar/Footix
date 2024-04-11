package com.example.footixappbachelorarbeit.viewModelLiveData

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewModelSettingsHandler: ViewModel() {


    val a: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().apply {
            value = false
        }
    }

    val b: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().apply {
            value = false
        }
    }
}