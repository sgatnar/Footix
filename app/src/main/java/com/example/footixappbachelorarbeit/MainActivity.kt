package com.example.footixappbachelorarbeit

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.footixappbachelorarbeit.databinding.ActivityMainBinding
import androidx.fragment.app.Fragment
import com.example.footixappbachelorarbeit.*
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.sleep(3000)
        installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(HomeFragment())

        binding.bottomNavigationView.changeColor(R.color.colorDefault, R.color.colorSelected)

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

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}