package com.example.newswave.presentation

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
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
import com.example.newswave.data.database.dbNews.UserPreferences
import com.example.newswave.databinding.ActivityMainBinding
import com.example.newswave.presentation.fragments.TopNewsFragment
import com.example.newswave.presentation.viewModels.SessionViewModel
import com.example.newswave.presentation.viewModels.ViewModelFactory
import com.example.newswave.utils.LocaleHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarMenuView
import com.jakewharton.threetenabp.AndroidThreeTen
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var bottomNavigationView: BottomNavigationView
//    private var backPressedOnce = false
//    private val backPressHandler = Handler(Looper.getMainLooper())

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val sessionViewModel: SessionViewModel by viewModels { viewModelFactory }


    private val component by lazy {
        (application as NewsApp).component
    }

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, UserPreferences(newBase).getInterfaceLanguage()))
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
    }


    fun setSelectedMenuItem(itemId: Int) {
        bottomNavigationView.menu.findItem(itemId).isChecked = true
    }
}