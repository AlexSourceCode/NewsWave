package com.example.newswave.presentation.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.newswave.R
import com.example.newswave.databinding.FragmentTopNewsBinding
import com.example.newswave.domain.NewsItemEntity
import com.example.newswave.presentation.Filter
import com.example.newswave.presentation.MainActivity
import com.example.newswave.presentation.adapters.NewsListAdapter
import com.example.newswave.presentation.viewModels.TopNewsViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch


class TopNewsFragment : Fragment() {

    private lateinit var binding: FragmentTopNewsBinding
    private lateinit var adapter: NewsListAdapter
    private val viewModel: TopNewsViewModel by viewModels()

    var selectedFilter: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTopNewsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        observeViewModel()
        setupTabLayout()
        selectedFilter = requireActivity().application.getString(Filter.TEXT.descriptionResId)
        searchByFilterListener()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isShowingSearchResults()) {
                        viewModel.loadTopNewsFromRoom()
                        binding.edSearch.text.clear()
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            })
    }



    fun scrollToTop() {
        binding.rcNews.scrollToPosition(0)
    }


    fun isShowingSearchResults(): Boolean {
        val sharedPreferences = requireActivity().application.getSharedPreferences(
            "news_by_search",
            Context.MODE_PRIVATE
        )
        val newsSearchResult = sharedPreferences.getString("news_search_result", null)
        return newsSearchResult != null
    }

    private fun setupTabLayout() {
        val tabLayout: TabLayout = binding.tabLayout


        tabLayout.addTab(tabLayout.newTab().setText(Filter.TEXT.descriptionResId))
        tabLayout.addTab(tabLayout.newTab().setText(Filter.AUTHOR.descriptionResId))
        tabLayout.addTab(tabLayout.newTab().setText(Filter.DATE.descriptionResId))





        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                //поменять как параметр поумолчания

                val context = requireActivity().application


                selectedFilter = Filter.entries.find { filter ->
                    context.getString(filter.descriptionResId) == tab.text.toString()
                }?.let { filter ->
                    context.getString(filter.descriptionResId)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun observeViewModel() {
        viewModel.newsList.observe(viewLifecycleOwner) {
            Log.d("checkadapter", "${it.map { it.id }}")
            adapter.submitList(it)
        }
    }

    private fun searchByFilterListener() {
        binding.edSearch.setOnKeyListener { view, keycode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keycode == KeyEvent.KEYCODE_ENTER) { // проверка, что нажатая клавиша является Enter
                selectedFilter?.let {
                    viewModel.setSearchParameters(
                        it,
                        binding.edSearch.text.toString()
                    )
                }
                lifecycleScope.launch {
                    viewModel.searchNewsByFilter()
                    viewModel.showNews()
                }
                true
            } else {
                false
            }
        }
    }

    private fun setupAdapter() {
        adapter = NewsListAdapter(requireActivity().application)
        binding.rcNews.adapter = adapter
        adapter.onNewsClickListener = {
            launchNewsDetailsFragment(it)
        }

        adapter.onLoadMoreListener = {
            viewModel.loadNewsForPreviousDay()
        }
    }


    private fun launchNewsDetailsFragment(news: NewsItemEntity) {
        findNavController().navigate(
            TopNewsFragmentDirections.actionTopNewsFragmentToNewsDetailsFragment(
                news
            )
        )
    }

}