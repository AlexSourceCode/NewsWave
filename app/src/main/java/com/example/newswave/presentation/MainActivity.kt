package com.example.newswave.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.newswave.R
import com.example.newswave.app.NewsApp
import com.example.newswave.databinding.ActivityMainBinding
import com.example.newswave.presentation.fragments.TopNewsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jakewharton.threetenabp.AndroidThreeTen

class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var bottomNavigationView: BottomNavigationView

    private val component by lazy {
        (application as NewsApp).component
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        component.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        AndroidThreeTen.init(this)

        val navController = findNavController(R.id.nav_host_fragment)
        bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setupWithNavController(navController)

        bottomNavigationView.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.topNewsFragment && item.isChecked) {
                val navHostFragment =
                    supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                val fragment = navHostFragment.childFragmentManager.fragments.firstOrNull()
                if (fragment is TopNewsFragment) {
                    fragment.scrollToTop()
                }
                true // Возвращаем true, чтобы предотвратить дальнейшую обработку
            } else {
                // Возвращаем true для всех элементов, чтобы позволить navController обрабатывать навигацию
                NavigationUI.onNavDestinationSelected(item, navController)
                true
            }
        }

    }

        fun setSelectedMenuItem(itemId: Int) {
            bottomNavigationView.menu.findItem(itemId).isChecked = true
        }


}