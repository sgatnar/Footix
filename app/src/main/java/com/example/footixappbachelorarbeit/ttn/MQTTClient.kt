package com.example.footixappbachelorarbeit.ttn

import android.content.Context
import android.util.Base64
import android.util.Log
import info.mqtt.android.service.MqttAndroidClient
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
    val PASSWORD_APIKEY_MQTT =
        "NNSXS.YKQELUZ3MNYEEWFD7BNQCORQDIQ52SKQRHTNQTQ.RVYLYOB7MIJ5LZS5HS2DD6ZFK6Z4QSNQJQZU4TWHEZYQP6RFRDOA" // APPKEY
    val TOPIC_UPLINK = "v3/footix-gnss-application@ttn/devices/eui-70b3d57ed0066110/up"
    val TOPIC_DOWNLINK = "v3/footix-gnss-application@ttn/devices/eui-70b3d57ed0066110/down/push"

    private lateinit var mqttAndroidClient: MqttAndroidClient
    private lateinit var dataListener: DataListener

    companion object {
        const val TAG = "AndroidMqttClient"
    }

    interface DataListener {
        fun onDataReceived(long: Double, lat: Double)
    }

    fun connect(callback: IMqttActionListener, listener: DataListener) {
        dataListener = listener

        mqttAndroidClient = MqttAndroidClient(context, SERVER_URL_MQTT, USERNAME_MQTT)
        mqttAndroidClient.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                handlePayload(message)
            }

            override fun connectionLost(cause: Throwable?) {
                Log.d(TAG, "Connection lost ${cause.toString()}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                Log.d(TAG, "Delivery completed ${token.toString()}")
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

    fun handlePayload(message: MqttMessage?) {
        try {
            val payload = String(message?.payload ?: return)
            val uplinkMessage = JSONObject(payload)
            Log.d(TAG, "Uplinkmessage: $uplinkMessage")
            val frmPayloadBase64 =
                uplinkMessage.getJSONObject("uplink_message").getString("frm_payload")
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

    fun publish(topic: String, data: JSONObject) {
        val encodedPayload: ByteArray
        try {
            encodedPayload = data.toString().toByteArray(charset("UTF-8"))
            val message = MqttMessage(encodedPayload)

            message.qos = 0
            message.isRetained = true
            val m = message.payload

            mqttAndroidClient.publish(topic, message, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "$message published to $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to publish $message to $topic")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
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
