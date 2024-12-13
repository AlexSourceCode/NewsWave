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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.newswave.R
import com.example.newswave.app.NewsApp
import com.example.newswave.databinding.FragmentTopNewsBinding
import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.domain.model.Filter
import com.example.newswave.presentation.MainActivity
import com.example.newswave.presentation.adapters.NewsListAdapter
import com.example.newswave.presentation.state.NewsState
import com.example.newswave.presentation.viewModels.SessionViewModel
import com.example.newswave.presentation.viewModels.TopNewsViewModel
import com.example.newswave.presentation.viewModels.ViewModelFactory
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


class TopNewsFragment : Fragment() {

    private lateinit var binding: FragmentTopNewsBinding
    private lateinit var adapter: NewsListAdapter
    private val topNewsViewModel: TopNewsViewModel by viewModels { viewModelFactory }
    private val sessionViewModel: SessionViewModel by activityViewModels { viewModelFactory }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val component by lazy {
        (requireActivity().application as NewsApp).component
    }

    // Переменные для управления состоянием поиска
    private var isSearchNews: Boolean? = null
    var selectedFilter: String? = null

    companion object{
        private const val REFRESH_REQUEST_KEY = "refresh_request"
    }

    override fun onAttach(context: Context) {
        component.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel()
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
        setupAdapter()              // Настройка адаптера для RecyclerView
        setupTabLayout()            // Настройка вкладок (TabLayout)
        selectedFilter = requireActivity().application.getString(Filter.TEXT.descriptionResId)
        searchByFilterListener()    // Добавление слушателя поиска
        handleBackNavigation()      // Обработка нажатия кнопки "Назад"
        setOnClickListener()        // Установка слушателей нажатий
        setupSwipeRefresh()         // Обновление данных при свайпе вниз
        parentFragmentManager.setFragmentResultListener(REFRESH_REQUEST_KEY, this) { _, _ ->
            topNewsViewModel.refreshData() // Повторный запрос данных
        }
    }

    private fun setOnClickListener() {
        binding.tvRetry.setOnClickListener {
            topNewsViewModel.refreshData()//?
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
                        topNewsViewModel.backToTopNews()
                        isSearchNews = false
                    } else {
                        // Обычное поведение кнопки "Назад"
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
                    topNewsViewModel.updateSearchParameters(it, binding.edSearch.text.toString())
                    adapter.submitList(emptyList())
                    isSearchNews = true
                }
                true
            } else {
                false
            }
        }
    }


    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            if (isSearchNews == true) {
                topNewsViewModel.searchNewsByFilter()
            } else {
                topNewsViewModel.refreshData()
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                topNewsViewModel.uiState.collect { uiState ->
                    when (uiState) {
                        is NewsState.Error -> {
                            binding.pgNews.visibility = View.GONE
                            when (uiState.message.toString().trim()) {
                                requireContext().getString(R.string.no_internet_connection) -> {
                                    if (adapter.currentList.isEmpty()) {
                                        binding.tvRetry.visibility = View.VISIBLE
                                    }
                                    binding.tvRetry.setOnClickListener {
                                        topNewsViewModel.refreshData() // Функция повторного запроса данных
                                    }
                                    showToast()
                                }

                                requireContext().getString(R.string.errorHTTP402) -> {
                                    if (adapter.currentList.isEmpty()) {
                                        binding.tvRetry.visibility = View.VISIBLE
                                    }
                                    binding.tvRetry.setOnClickListener {
                                        topNewsViewModel.refreshData() // Функция повторного запроса данных
                                    }
                                    showToast()
                                }

                                requireContext().getString(R.string.news_list_is_empty_or_invalid_parameters).trim() -> {
                                    binding.tvRetry.visibility = View.GONE
                                    binding.tvErrorAvailableNews.text = requireContext().getString(R.string.no_news_for_criteria)
                                    binding.tvErrorAvailableNews.visibility = View.VISIBLE
                                }
                                requireContext().getString(R.string.errorMessageNoResultsFound) -> {
                                    binding.tvRetry.visibility = View.GONE
                                    binding.tvErrorAvailableNews.text = requireContext().getString(R.string.errorMessageNoResultsFound)
                                    binding.tvErrorAvailableNews.visibility = View.VISIBLE
                                }
                                requireContext().getString(R.string.error_no_internet_in_search) -> {
                                    showToast()
                                    binding.tvRetry.visibility = View.VISIBLE
                                    binding.tvRetry.setOnClickListener {
                                        topNewsViewModel.searchNewsByFilter()
                                    }
                                }


                                else -> { // Другие ошибки
                                    showToast()
                                    binding.tvRetry.setOnClickListener {
                                        topNewsViewModel.refreshData()
                                    }
                                }
                            }
                        }

                        is NewsState.Loading -> {
                            binding.tvErrorAvailableNews.visibility = View.GONE
                            binding.tvRetry.visibility = View.GONE
                            binding.pgNews.visibility = View.VISIBLE
                        }

                        is NewsState.Success -> {
                            binding.tvErrorAvailableNews.visibility = View.GONE
                            binding.pgNews.visibility = View.GONE
                            binding.tvRetry.visibility = View.GONE
                            if (!adapter.shouldHideRetryButton) {
                                adapter.submitListWithLoadMore(uiState.currentList, null)
                                adapter.notifyDataSetChanged() //crutch
                            } else {
                                adapter.submitList(uiState.currentList) {
                                    if (topNewsViewModel.isFirstLaunch) {
                                        lifecycleScope.launch {
                                            delay(1000)
                                            scrollToTop()
                                            topNewsViewModel.isFirstLaunch = false
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) { //started?
                sessionViewModel.refreshEvent.collect {
                    topNewsViewModel.refreshData()
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
            topNewsViewModel.loadNewsForPreviousDay()
        }
    }

    private fun showToast() {    // Показ уведомления
        Toast.makeText(
            requireContext(),
            requireActivity().getString(R.string.error_load_data),
            Toast.LENGTH_SHORT
        ).show()
    }

    fun scrollToTop() {         // Прокрутка к началу списка новостей
        binding.rcNews.scrollToPosition(0)
    }

    private fun launchNewsDetailsFragment(news: NewsItemEntity) {   // Переход к фрагменту с деталями новости
        val author = news.author.split(",")[0]
        topNewsViewModel.preloadAuthorData(author)
        findNavController().navigate(
            TopNewsFragmentDirections.actionTopNewsFragmentToNewsDetailsFragment(news, null, R.id.topNewsFragment)
        )
    }
}