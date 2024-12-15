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

    companion object {
        private const val REFRESH_REQUEST_KEY = "refresh_request"
    }

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
        observeViewModel()
        initializeUI()
        setupFragmentResultListener()
    }

    private fun setupFragmentResultListener() {
        parentFragmentManager.setFragmentResultListener(REFRESH_REQUEST_KEY, this) { _, _ ->
            topNewsViewModel.refreshData()
        }
    }

    private fun initializeUI() {
        setupAdapter()
        setupTabLayout()
        selectedFilter = getString(Filter.TEXT.descriptionResId)
        setupSearchListener()
        handleBackNavigation()
        setupClickListeners()
        setupSwipeRefresh()
    }

    private fun setupClickListeners() {
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
                        exitSearchMode()
                    } else {
                        // Обычное поведение кнопки "Назад"
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            })
    }

    // Очистка поля поиска и возврат к основным новостям
    private fun exitSearchMode() {
        binding.edSearch.text.clear()
        topNewsViewModel.backToTopNews()
        isSearchNews = false
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

    private fun setupSearchListener() {
        binding.edSearch.setOnKeyListener { view, keycode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keycode == KeyEvent.KEYCODE_ENTER) { // проверка, что нажатая клавиша является Enter
                executeSearch()
                true
            } else {
                false
            }
        }
    }

    private fun executeSearch() {
        selectedFilter?.let {
            topNewsViewModel.updateSearchParameters(it, binding.edSearch.text.toString())
            adapter.submitList(emptyList())
            isSearchNews = true
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
                    handleUiState(uiState)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                sessionViewModel.refreshEvent.collect { topNewsViewModel.refreshData() }
            }
        }
    }

    private fun handleUiState(uiState: NewsState) {
        when (uiState) {
            is NewsState.Error -> handleErrorState(uiState)
            is NewsState.Loading -> handleLoadingState()
            is NewsState.Success -> handleSuccessState(uiState)
        }
    }

    private fun handleErrorState(uiState: NewsState.Error) {
        binding.pgNews.visibility = View.GONE
        val refreshNews = if (isSearchNews == true){
            {
                topNewsViewModel.searchNewsByFilter()
            }
        } else{
            {
                topNewsViewModel.refreshData()
                topNewsViewModel.showTopNews()
            }
        }
        when (uiState.message.trim()) {
            getString(R.string.no_internet_connection),
            getString(R.string.errorHTTP402) -> {
                showRetryOption (refreshNews)
            }
            getString(R.string.news_list_is_empty_or_invalid_parameters) -> {
                showErrorMessage(getString(R.string.no_news_for_criteria))
            }
            getString(R.string.errorMessageNoResultsFound) -> {
                showErrorMessage(getString(R.string.errorMessageNoResultsFound))
            }

            else -> {
                showRetryOption (refreshNews)
            }
        }
    }

    private fun showRetryOption(retryAction: () -> Unit) {
        binding.tvRetry.visibility = if (isSearchNews == true || adapter.currentList.isEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }
        binding.tvRetry.setOnClickListener { retryAction() }
        showToast()
    }

    private fun showErrorMessage(message: String) {
        binding.tvRetry.visibility = View.GONE
        binding.tvErrorAvailableNews.text = message
        binding.tvErrorAvailableNews.visibility = View.VISIBLE
    }

    private fun handleLoadingState() {
        binding.tvErrorAvailableNews.visibility = View.GONE
        binding.tvRetry.visibility = View.GONE
        binding.pgNews.visibility = View.VISIBLE
    }

    private fun handleSuccessState(uiState: NewsState.Success) {
        binding.pgNews.visibility = View.GONE
        binding.tvErrorAvailableNews.visibility = View.GONE
        binding.tvRetry.visibility = View.GONE

        if (!adapter.shouldHideRetryButton) {
            adapter.submitListWithLoadMore(uiState.currentList, null)
            adapter.notifyDataSetChanged()
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

    private fun setupAdapter() {
        adapter = NewsListAdapter(requireActivity().application).apply {
            onNewsClickListener = { launchNewsDetailsFragment(it) }
            onLoadMoreListener = {
                adapter.shouldHideRetryButton = false
                topNewsViewModel.loadNewsForPreviousDay()
            }
        }
        binding.rcNews.adapter = adapter
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
            TopNewsFragmentDirections.actionTopNewsFragmentToNewsDetailsFragment(
                news, null, R.id.topNewsFragment
            )
        )
    }
}