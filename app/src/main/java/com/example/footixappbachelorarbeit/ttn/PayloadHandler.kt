package com.example.footixappbachelorarbeit.ttn

import android.util.Base64
import android.util.Log
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.ByteOrder

class PayloadHandler {

    companion object {
        const val TAG = "PayloadHandler"
    }

    interface DataListener {
        fun onDataReceived(long: Double, lat: Double)
    }

    fun handlePayload(message: MqttMessage?, dataListener: DataListener) {
        try {
            val payload = String(message?.payload ?: return)
            val uplinkMessage = JSONObject(payload)
            Log.d(TAG, "Uplinkmessage: $uplinkMessage")
            val frmPayloadBase64 = uplinkMessage.getJSONObject("uplink_message").getString("frm_payload")
            Log.d(TAG, "Payload: $frmPayloadBase64")

            val frmPayloadBytes = Base64.decode(frmPayloadBase64, Base64.DEFAULT)

            if (frmPayloadBytes.size == 16) {
                val longitudeBytes = frmPayloadBytes.copyOfRange(8, 16)
                val latitudeBytes = frmPayloadBytes.copyOfRange(0, 8)

                val longitudeBuffer = ByteBuffer.wrap(longitudeBytes).order(ByteOrder.LITTLE_ENDIAN)
                val latitudeBuffer = ByteBuffer.wrap(latitudeBytes).order(ByteOrder.LITTLE_ENDIAN)

                val receivedLongitude = longitudeBuffer.double
                Log.d(TAG, "receivedLongitude: $receivedLongitude")
                val receivedLatitude = latitudeBuffer.double
                Log.d(TAG, "receivedLatitude: $receivedLatitude")

                dataListener.onDataReceived(receivedLongitude, receivedLatitude)
            } else {
                Log.e(TAG, "Error decoding payload")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Error processing MQTT message: ${e.message}")
        }
    }
}
