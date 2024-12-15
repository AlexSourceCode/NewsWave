package com.example.newswave.presentation

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.newswave.R
import com.example.newswave.app.NewsApp
import com.example.newswave.data.dataSource.local.UserPreferences
import com.example.newswave.databinding.ActivityMainBinding
import com.example.newswave.presentation.fragments.TopNewsFragment
import com.example.newswave.presentation.viewModels.SessionViewModel
import com.example.newswave.presentation.viewModels.ViewModelFactory
import com.example.newswave.utils.LocaleHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val sessionViewModel: SessionViewModel by viewModels { viewModelFactory }
    private lateinit var bottomNavigationView: BottomNavigationView

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

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
        lifecycleScope.launch {
            sessionViewModel.activeMenuItemId.collect { itemId ->
                itemId?.let {
                    binding.bottomNavigationView.menu.findItem(it).isChecked = true
                }
            }
        }
    }


    fun setSelectedMenuItem(itemId: Int) {
        sessionViewModel.setActiveMenuItemId(itemId)
    }
}