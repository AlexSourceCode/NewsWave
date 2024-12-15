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

/**
 * Этот фрагмент взаимодействует с ViewModel для управления состоянием интерфейса и обработки пользовательских действий
 */
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

    // Текущий выбранный фильтр
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

    // Настраивает слушатель для получения результата обновления данных от других фрагментов
    private fun setupFragmentResultListener() {
        parentFragmentManager.setFragmentResultListener(REFRESH_REQUEST_KEY, this) { _, _ ->
            topNewsViewModel.refreshData()
        }
    }

    // Инициализирует пользовательский интерфейс, включая адаптер списка,
    // табы фильтров, обработку кнопки "Назад" и другие элементы.
    private fun initializeUI() {
        setupAdapter()
        setupTabLayout()
        selectedFilter = getString(Filter.TEXT.descriptionResId)
        setupSearchListener()
        handleBackNavigation()
        setupClickListeners()
        setupSwipeRefresh()
    }

    // Настраивает действия для кнопки повторного запроса
    private fun setupClickListeners() {
        binding.tvRetry.setOnClickListener {
            topNewsViewModel.refreshData()//?
        }
    }

    // Настраивает обработку кнопки "Назад"
    // Если активен режим поиска, происходит выход из него, иначе работает стандартное поведение
    private fun handleBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (topNewsViewModel.isInSearchMode.value) {
                        exitSearchMode()
                    } else {
                        // Обычное поведение кнопки "Назад"
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            })
    }

    // Выход из режима поиска: очищает поле поиска и возвращает основной список новостей
    private fun exitSearchMode() {
        binding.edSearch.text.clear()
        topNewsViewModel.backToTopNews()
    }

    // Настраивает вкладки с фильтрами и добавляет слушатель выбора вкладки
    private fun setupTabLayout() {
        val tabLayout: TabLayout = binding.tabLayout
        Filter.entries.forEach { filter ->
            tabLayout.addTab(tabLayout.newTab().setText(filter.descriptionResId))
        }
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectedFilter = Filter.entries.find { filter ->
                    getString(filter.descriptionResId) == tab.text.toString()
                }?.let { filter ->
                    getString(filter.descriptionResId)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    // Настраивает обработку нажатия в поле поиска
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

    // Выполняет поиск новостей по текущему фильтру и введённому тексту
    private fun executeSearch() {
        selectedFilter?.let {
            topNewsViewModel.updateSearchParameters(it, binding.edSearch.text.toString())
            adapter.submitList(emptyList())
        }
    }

    // Настраивает обновление данных свайпом
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            if (topNewsViewModel.isInSearchMode.value) {
                topNewsViewModel.searchNewsByFilter()
            } else {
                topNewsViewModel.refreshData()
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    // Настраивает адаптер для отображения списка новостей
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

    // Подписка на изменения состояния ViewModel и обновление UI
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

    // Обрабатывает изменения состояния новостей и обновляет интерфейс
    private fun handleUiState(uiState: NewsState) {
        when (uiState) {
            is NewsState.Error -> handleErrorState(uiState)
            is NewsState.Loading -> handleLoadingState()
            is NewsState.Success -> handleSuccessState(uiState)
        }
    }

    // Обрабатывает состояние ошибки, показывая соответствующие сообщения и опции
    private fun handleErrorState(uiState: NewsState.Error) {
        binding.pgNews.visibility = View.GONE
        val refreshNews = if (topNewsViewModel.isInSearchMode.value) {
            { topNewsViewModel.searchNewsByFilter() }
        } else {
            {
                topNewsViewModel.refreshData()
                topNewsViewModel.showTopNews()
            }
        }
        when (uiState.message.trim()) {
            getString(R.string.no_internet_connection),
            getString(R.string.errorHTTP402) -> {
                showRetryOption(refreshNews)
            }

            getString(R.string.news_list_is_empty_or_invalid_parameters) -> {
                showErrorMessage(getString(R.string.no_news_for_criteria))
            }

            getString(R.string.errorMessageNoResultsFound) -> {
                showErrorMessage(getString(R.string.errorMessageNoResultsFound))
            }

            else -> {
                showRetryOption(refreshNews)
            }
        }
    }

    // Показывает опцию повторной загрузки данных
    private fun showRetryOption(retryAction: () -> Unit) {
        binding.tvRetry.visibility =
            if (topNewsViewModel.isInSearchMode.value || adapter.currentList.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
        binding.tvRetry.setOnClickListener { retryAction() }
        showToast()
    }

    // Отображение сообщения об ошибке на экране
    private fun showErrorMessage(message: String) {
        binding.tvRetry.visibility = View.GONE
        binding.tvErrorAvailableNews.text = message
        binding.tvErrorAvailableNews.visibility = View.VISIBLE
    }

    // Управление состоянием загрузки
    private fun handleLoadingState() {
        binding.tvErrorAvailableNews.visibility = View.GONE
        binding.tvRetry.visibility = View.GONE
        binding.pgNews.visibility = View.VISIBLE
    }

    // Обработка состояния успешной загрузки
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

    // Показ уведомления о загрузке данных
    private fun showToast() {    // Показ уведомления
        Toast.makeText(
            requireContext(),
            requireActivity().getString(R.string.error_load_data),
            Toast.LENGTH_SHORT
        ).show()
    }

    // Прокрутка RecyclerView к началу списка новостей
    fun scrollToTop() {         // Прокрутка к началу списка новостей
        binding.rcNews.scrollToPosition(0)
    }

    // Открытие фрагмента с деталями новости
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