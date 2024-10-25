package com.example.newswave.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
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
//                val navOptions = NavOptions.Builder()
//                    .setEnterAnim(R.anim.fade_in)
//                    .setExitAnim(R.anim.fade_out)
//                    .setPopEnterAnim(R.anim.fade_in)
//                    .setPopExitAnim(R.anim.fade_out)
//                    .build()
                // Возвращаем true для всех элементов, чтобы позволить navController обрабатывать навигацию
                NavigationUI.onNavDestinationSelected(item, navController)
//                navController.navigate(item.itemId, null, navOptions)
                true
            }
        }

    }

        fun setSelectedMenuItem(itemId: Int) {
            bottomNavigationView.menu.findItem(itemId).isChecked = true
        }


}