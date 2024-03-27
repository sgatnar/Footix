package com.example.footixappbachelorarbeit

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import com.example.footixappbachelorarbeit.databinding.ActivityMainBinding
import com.example.footixappbachelorarbeit.ttn.MQTTClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttConnectOptions

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mqttClient: MQTTClient
    val TOPIC_UPLINK = "v3/footix-gnss-application@ttn/devices/eui-70b3d57ed0066110/up"
    val TOPIC_DOWNLINK = "v3/footix-gnss-application@ttn/devices/eui-70b3d57ed0066110/down/push"
    var subscribedUplink: Boolean = false
    var subscribedDownlink: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        changeStatusBarColor()
        replaceFragment(HomeFragment())

        binding.bottomNavigationView.changeColor(R.color.colorDefault, R.color.colorSelected)
        binding.bottomNavigationView.selectedItemId = R.id.home

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    replaceFragment(HomeFragment())
                }
                R.id.session -> {
                    replaceFragment(SessionFragment())
                }
                R.id.settings -> {
                    replaceFragment(SettingsFragment())
                }
            }
            true
        }

        mqttClient = MQTTClient(this)
        mqttClient.connect(object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.d("MQTT", "Connection successful...")
                if(!subscribedUplink) {
                    mqttClient.subscribeToTopic(TOPIC_UPLINK, 0)
                }
                if (!subscribedDownlink){
                    mqttClient.subscribeToTopic(TOPIC_DOWNLINK, 0)
                }
                val snackbar = Snackbar.make(
                    findViewById(R.id.fragment_home), // Replace with your layout ID
                    resources.getString(R.string.toastConnectionToMQTT),
                    Snackbar.LENGTH_SHORT
                ).setBackgroundTint(resources.getColor(R.color.black_footix, null)) // Optional: Set success color

                snackbar.show()
            }
            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.e("MQTT", "Connection failure", exception)
                if (exception != null) {
                    Toast.makeText(this@MainActivity, "MQTT connection failed: " + exception?.message.toString(), Toast.LENGTH_SHORT).show()
                };
            }
        })
    }

    companion object {
        const val TAG = "AndroidMqttClient"
    }

    private fun BottomNavigationView.changeColor(@ColorRes defaultColor: Int, @ColorRes selectedColor: Int) {
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_pressed),
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(
                ContextCompat.getColor(context, defaultColor),
                ContextCompat.getColor(context, selectedColor),
                ContextCompat.getColor(context, defaultColor)
            )
        )
        itemIconTintList = colorStateList
        itemTextColor = colorStateList
    }
    private fun changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = resources.getColor(R.color.grey_toolbar_footix, theme)
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}