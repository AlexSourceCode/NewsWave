package com.example.newswave.presentation.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.newswave.databinding.FragmentTopNewsBinding
import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.utils.Filter
import com.example.newswave.app.NewsApp
import com.example.newswave.domain.model.State
import com.example.newswave.presentation.adapters.NewsListAdapter
import com.example.newswave.presentation.viewModels.TopNewsViewModel
import com.example.newswave.presentation.viewModels.ViewModelFactory
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject


class TopNewsFragment : Fragment() {

    private lateinit var binding: FragmentTopNewsBinding
    private lateinit var adapter: NewsListAdapter

    private lateinit var viewModel: TopNewsViewModel
    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val component by lazy {
        (requireActivity().application as NewsApp).component
    }


    var selectedFilter: String? = null

    override fun onAttach(context: Context) {
        component.inject(this)
        super.onAttach(context)
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
        viewModel = ViewModelProvider(this, viewModelFactory)[TopNewsViewModel::class.java]
        setupAdapter()
        observeViewModel()

        setupTabLayout()
        selectedFilter = requireActivity().application.getString(Filter.TEXT.descriptionResId)
        searchByFilterListener()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.edSearch.text.toString() != "") {
                        binding.edSearch.text.clear()
                        viewModel.backToTopNews()
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            })

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

    private fun searchByFilterListener() {
        binding.edSearch.setOnKeyListener { view, keycode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keycode == KeyEvent.KEYCODE_ENTER) { // проверка, что нажатая клавиша является Enter
                selectedFilter?.let {
                    viewModel.setSearchParameters(
                        it,
                        binding.edSearch.text.toString()
                    )
                }
                viewModel.searchNewsByFilter()
                true
            } else {
                false
            }
        }
    }

    private fun observeViewModel() { // What name to give a feature?
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED){// ПОЧЕМУ ДЖАЖДЫ А ПОТОМ УВЕЛИЧЕНИЕ НА +1 ПРИ created norm
                viewModel.uiState.collect{ uiState ->
                    Log.d("LastState", uiState.toString())
                    when(uiState){
                        is State.Error -> Log.d("CheckState", uiState.toString())
                        is State.Loading -> {
                            binding.pgNews.visibility = View.VISIBLE
                        }
                        is State.Success -> {
                            binding.pgNews.visibility = View.GONE
                            adapter.submitList(uiState.currentList)
                        }
                        else -> {Log.d("CheckState", "FAILED")}
                    }
                }
            }
        }
    }

    private fun setupAdapter() {
        adapter = NewsListAdapter(requireActivity().application)
        binding.rcNews.adapter = adapter
        adapter.onNewsClickListener = {
            launchNewsDetailsFragment(it)
        }

//        adapter.onLoadMoreListener = {
//            viewModel.loadNewsForPreviousDay()
//        }
    }

    fun scrollToTop() {
        binding.rcNews.scrollToPosition(0)
    }

    private fun launchNewsDetailsFragment(news: NewsItemEntity) {
        findNavController().navigate(
            TopNewsFragmentDirections.actionTopNewsFragmentToNewsDetailsFragment(news, null)
        )
    }

}