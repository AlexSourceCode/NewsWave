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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newswave.R
import com.example.newswave.app.NewsApp
import com.example.newswave.databinding.FragmentTopNewsBinding
import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.domain.model.NewsState
import com.example.newswave.presentation.MainActivity
import com.example.newswave.presentation.adapters.NewsListAdapter
import com.example.newswave.presentation.viewModels.SavedStateViewModelFactory
import com.example.newswave.presentation.viewModels.TopNewsViewModel
import com.example.newswave.presentation.viewModels.ViewModelFactory
import com.example.newswave.utils.Filter
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject


class TopNewsFragment : Fragment() {

    private lateinit var binding: FragmentTopNewsBinding
    private lateinit var adapter: NewsListAdapter

    private lateinit var viewModel: TopNewsViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

//    @Inject
//    lateinit var viewModelFactory: SavedStateViewModelFactory

    private val component by lazy {
        (requireActivity().application as NewsApp).component
    }

    // Переменные для управления состоянием поиска
    private var isSearchNews: Boolean? = null
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
        (activity as MainActivity).setSelectedMenuItem(R.id.topNewsFragment)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory)[TopNewsViewModel::class.java]
        setupAdapter()              // Настройка адаптера для RecyclerView
        observeViewModel()          // Подписка на обновления данных из ViewModel
        setupTabLayout()            // Настройка вкладок (TabLayout)
        selectedFilter = requireActivity().application.getString(Filter.TEXT.descriptionResId)
        searchByFilterListener()    // Добавление слушателя поиска
        handleBackNavigation()      // Обработка нажатия кнопки "Назад"
        setOnClickListener()        // Установка слушателей нажатий
        setupSwipeRefresh()         // Обновление данных при свайпе вниз
    }

    private fun setOnClickListener() {
        binding.tvRetry.setOnClickListener {
            viewModel.searchNewsByFilter()
        }
    }

    private fun handleBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isSearchNews == true) {
                        // Очистка поля поиска и возврат к основным новостям
                        binding.edSearch.text.clear() //handleBackFromSearch
                        viewModel.backToTopNews()
                        isSearchNews = false
                    } else {
                        // Обычное поведение кнопки "Назад"
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            })
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            if (isSearchNews == true) {
                viewModel.searchNewsByFilter()
            } else {
                viewModel.refreshData()
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun setupTabLayout() {
        val tabLayout: TabLayout = binding.tabLayout
        tabLayout.addTab(tabLayout.newTab().setText(Filter.TEXT.descriptionResId))
        tabLayout.addTab(tabLayout.newTab().setText(Filter.AUTHOR.descriptionResId))
        tabLayout.addTab(tabLayout.newTab().setText(Filter.DATE.descriptionResId))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
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
                    viewModel.updateSearchParameters(it, binding.edSearch.text.toString())
                    adapter.submitList(emptyList())
                    isSearchNews = true
                }
                true
            } else {
                false
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.uiState.collect { uiState ->
                    when (uiState) {
                        is NewsState.Error -> {
                            Log.d("StateUiState", uiState.toString())
                            showToast()
                            binding.pgNews.visibility = View.GONE
                            if (isSearchNews == true) {
                                Log.d("StateUiState", "isSearchNews")
                                binding.tvRetry.visibility = View.VISIBLE
                                adapter.submitList(emptyList())
                            }
                        }

                        is NewsState.Loading -> {
                            Log.d("StateUiState", uiState.toString())
                            binding.tvRetry.visibility = View.GONE
                            binding.pgNews.visibility = View.VISIBLE
                        }

                        is NewsState.Success -> {
                            Log.d("StateUiState", "Success")
                            binding.pgNews.visibility = View.GONE
                            binding.tvRetry.visibility = View.GONE
                            if (!adapter.shouldHideRetryButton) {
                                adapter.submitListWithLoadMore(uiState.currentList, null)
                                adapter.notifyDataSetChanged() //crutch
                            } else {
                                adapter.submitList(uiState.currentList) {
                                    if (viewModel.isFirstLaunch) {
                                        lifecycleScope.launch {
                                            delay(700)
                                            scrollToTop()
                                            viewModel.isFirstLaunch = false
                                        }
                                    }
                                }

                            }
                        }
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

        adapter.onLoadMoreListener = {
            adapter.shouldHideRetryButton = false
            viewModel.loadNewsForPreviousDay()
        }
    }

    private fun showToast() {    // Показ уведомления
        Toast.makeText(
            requireContext(),
            requireActivity().getString(R.string.error_load_data),
            Toast.LENGTH_LONG
        ).show()
    }


    fun scrollToTop() {         // Прокрутка к началу списка новостей
        binding.rcNews.scrollToPosition(0)
    }

    private fun launchNewsDetailsFragment(news: NewsItemEntity) {   // Переход к фрагменту с деталями новости
        val author = news.author.split(",")[0]
        viewModel.preloadAuthorData(author)
        findNavController().navigate(
            TopNewsFragmentDirections.actionTopNewsFragmentToNewsDetailsFragment(news, null)
        )
    }

}