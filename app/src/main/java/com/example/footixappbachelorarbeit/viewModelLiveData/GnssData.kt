package com.example.footixappbachelorarbeit.viewModelLiveData
data class GNSSPosition(var latitude: Float,
                    var lonitude: Float,
                    var distance: Float)

data class RealTimeGNSSData(var time: String,
                           var latitude: Float,
                           var lonitude: Float,
                           var distance: Float)