package com.example.footixappbachelorarbeit.ttn

import android.app.Application
import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.footixappbachelorarbeit.viewModelLiveData.ViewModelFragmentHandler
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MQTTClient(val context: Context) {

    //MQTT credentials
    val SERVER_URL_MQTT = "tcp://eu1.cloud.thethings.network:1883"
    val USERNAME_MQTT = "footix-gnss-application@ttn"
    val PASSWORD_APIKEY_MQTT = "NNSXS.ZED6RVBM27YAXGNKSIBQCU3FT7MXNRJNJ2TT6HY.KQ2XC6JAZQSCLJFW3KJWYW5F4WVJNXAVTJAXM7TISWGL6VWPARAA"
    val TOPIC_UPLINK = "v3/footix-gnss-application@ttn/devices/eui-70b3d57ed0066110/up"
    val TOPIC_DOWNLINK = "v3/footix-gnss-application@ttn/devices/eui-70b3d57ed0066110/down/push"

    private lateinit var mqttAndroidClient: MqttAndroidClient
    private lateinit var dataListener: DataListener

    companion object {
        const val TAG = "AndroidMqttClient"
    }

    fun connect(callback: IMqttActionListener, listener: DataListener) {
        dataListener = listener
        mqttAndroidClient = MqttAndroidClient(context, SERVER_URL_MQTT, USERNAME_MQTT)
        mqttAndroidClient.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                if (topic != null && message != null) {
                    try {
                        val payload = String(message.payload)
                        val uplinkMessage = JSONObject(payload)
                        Log.d(TAG, "Received uplink message from TTN: $uplinkMessage")
                        val frmPayloadBase64 = uplinkMessage.getJSONObject("uplink_message").getString("frm_payload")
                        //val timestampTTN = uplinkMessage.getJSONObject("rx_metadata").getString("time")
                        //Log.d(TAG, "Received uplink message from TTN: $timestampTTN")
                        val frmPayloadBytes = Base64.decode(frmPayloadBase64, Base64.DEFAULT)

                        if (frmPayloadBytes.size == 16) {  // Adjusted size to 12
                            val longitudeBytes = frmPayloadBytes.copyOfRange(8, 16)
                            val latitudeBytes = frmPayloadBytes.copyOfRange(0, 8)

                            val longitudeBuffer = ByteBuffer.wrap(longitudeBytes).order(ByteOrder.LITTLE_ENDIAN)
                            val latitudeBuffer = ByteBuffer.wrap(latitudeBytes).order(ByteOrder.LITTLE_ENDIAN)

                            val receivedLongitude = longitudeBuffer.double
                            val receivedLatitude = latitudeBuffer.double

                            dataListener.onDataReceived(receivedLongitude, receivedLatitude)

                        } else {
                            Log.e(TAG, "Invalid payload size")
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing JSON or decoding frm_payload: ${e.message}")
                    }
                }
            }

            override fun connectionLost(cause: Throwable?) {
                Log.d(TAG, "Connection lost ${cause.toString()}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
            }
        })

        val options = MqttConnectOptions()
        options.setWill(TOPIC_DOWNLINK, byteArrayOf(1, 2, 3, 4), 0, true)
        options.userName = USERNAME_MQTT
        options.password = PASSWORD_APIKEY_MQTT.toCharArray()

        try {
            mqttAndroidClient.connect(options, null, callback)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    interface DataListener {
        fun onDataReceived(long: Double, lat: Double)
    }

    fun subscribeToTopic(topic: String, qos: Int = 0) {
        try {
            mqttAndroidClient.subscribe(topic, qos, this, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Subscribed to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to subscribe $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun publishMessage(topic: String, payload: String) {
        try {
            val msg = MqttMessage()
            msg.payload = payload.toByteArray()
            if (mqttAndroidClient.isConnected) {
                mqttAndroidClient.publish(topic, msg.payload, 0, true)
                Log.d(TAG, "$msg published to $topic")
            }
        } catch (e: MqttException) {
            Log.d(TAG, "Error Publishing to $topic: " + e.message)
            e.printStackTrace()
        }
    }

    fun publish(topic: String, msg: String, qos: Int = 0, retained: Boolean = true) {
        try {
            val message = MqttMessage()
            message.payload = msg.toByteArray()
            message.qos = qos
            message.isRetained = retained
            mqttAndroidClient.publish(topic, message, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "$msg published to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to publish $msg to $topic")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        try {
            mqttAndroidClient.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Disconnected")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to disconnect")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }
}