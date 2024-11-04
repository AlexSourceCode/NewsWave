package com.example.newswave.presentation

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.newswave.R
import com.example.newswave.app.NewsApp
import com.example.newswave.databinding.ActivityMainBinding
import com.example.newswave.presentation.fragments.TopNewsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarMenuView
import com.jakewharton.threetenabp.AndroidThreeTen

class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var bottomNavigationView: BottomNavigationView
//    private var backPressedOnce = false
//    private val backPressHandler = Handler(Looper.getMainLooper())

    private val component by lazy {
        (application as NewsApp).component
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        component.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        AndroidThreeTen.init(this)


        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        bottomNavigationView = binding.bottomNavigationView
        bottomNavigationView.setupWithNavController(navController)




//        navController.navigate(R.id.subscribedAuthorsFragment, null)
//        navController.navigate(R.id.settingsFragment, null)
//        navController.navigate(R.id.topNewsFragment, null)

//        bottomNavigationView.setOnItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.topNewsFragment -> {
//                    navController.popBackStack(R.id.topNewsFragment, false)
//                }
//
//                else -> {
//                    if (navController.currentDestination?.id != item.itemId) {
//                        val navOptions = NavOptions.Builder()
//                            .setPopUpTo(item.itemId, false)
//                            .setLaunchSingleTop(true)
//                            .build()
//                        navController.navigate(item.itemId, null, navOptions)
//                    }
//                }
//            }
//            true
//        }


        bottomNavigationView.setOnItemReselectedListener { item ->
            if (item.itemId == R.id.topNewsFragment) {
                if (item.isChecked) {
                    val fragment = navHostFragment.childFragmentManager.fragments.firstOrNull()
                    if (fragment is TopNewsFragment) {
                        fragment.scrollToTop()
                    }
                }
            }
        }

//        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                if (backPressedOnce) {
//                    finish()
//                } else {
//                    backPressedOnce = true
//                    Toast.makeText(
//                        this@MainActivity,
//                        "Нажмите еще раз, чтобы выйти",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    backPressHandler.postDelayed({ backPressedOnce = false }, 2000)
//                }
//            }
//        })

    }


    fun setSelectedMenuItem(itemId: Int) {
        bottomNavigationView.menu.findItem(itemId).isChecked = true
    }
}