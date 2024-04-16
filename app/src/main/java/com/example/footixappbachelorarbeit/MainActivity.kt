package com.example.footixappbachelorarbeit

import android.content.Context
import android.content.res.ColorStateList
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import com.example.footixappbachelorarbeit.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        changeStatusBarColor()
        replaceFragment(HomeFragment())

        binding.bottomNavigationView.changeColor(R.color.colorDefaultNavBar, R.color.colorSelectedNavBar)
        binding.bottomNavigationView.selectedItemId = R.id.home

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    checkInternetAndNavigateTo(HomeFragment())
                }
                R.id.session -> {
                    checkInternetAndNavigateTo(SessionFragment())
                }
                R.id.settings -> {
                    checkInternetAndNavigateTo(SettingsFragment())
                }
            }
            true
        }
    }

    private fun checkInternetAndNavigateTo(fragment: Fragment) {
        if (isInternetAvailable()) {
            replaceFragment(fragment)
        } else {
            showNoInternetPopup()
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    private fun showNoInternetPopup() {
        val dialogView = layoutInflater.inflate(R.layout.standard_popup_layout_3, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val popupNoInternetTitle = dialogView.findViewById<TextView>(R.id.popupTitle)
        popupNoInternetTitle.text = getString(R.string.noInternet)

        val popupNoInternetText= dialogView.findViewById<TextView>(R.id.popupText)
        popupNoInternetText.text = getString(R.string.ConnectToInternet)

        val retryInternetConnectionButton = dialogView.findViewById<Button>(R.id.cancelButton)
        retryInternetConnectionButton.text = getString(R.string.retry)
        retryInternetConnectionButton.setOnClickListener {
            if (isInternetAvailable()) {
                dialog.dismiss()
                checkInternetAndNavigateTo(HomeFragment()) // Retry to check the internet connection
            } else {
                Toast.makeText(this, R.string.pleaseConnectAgain, Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    fun BottomNavigationView.changeColor(@ColorRes defaultColor: Int, @ColorRes selectedColor: Int) {
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
        window.statusBarColor = resources.getColor(R.color.grey_toolbar_footix, theme)
    }
    fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}