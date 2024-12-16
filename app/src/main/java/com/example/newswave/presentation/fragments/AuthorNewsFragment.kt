package com.example.newswave.presentation.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.newswave.R
import com.example.newswave.app.NewsApp
import com.example.newswave.databinding.FragmentAuthorNewsBinding
import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.presentation.MainActivity
import com.example.newswave.presentation.adapters.NewsListAdapter
import com.example.newswave.presentation.state.AuthState
import com.example.newswave.presentation.state.NewsState
import com.example.newswave.presentation.viewModels.AuthorNewsViewModel
import com.example.newswave.presentation.viewModels.ViewModelFactory
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Фрагмент для отображения новостей, связанных с конкретным автором.
 */
class AuthorNewsFragment : Fragment() {

    private var _binding: FragmentAuthorNewsBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<AuthorNewsFragmentArgs>() // Навигационные аргументы фрагмента, содержащий автора

    private lateinit var adapter: NewsListAdapter // Адаптер для списка новостей
    private val viewModel: AuthorNewsViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val component by lazy {
        (requireActivity().application as NewsApp).component
    }

    override fun onAttach(context: Context) {
        component.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAuthorNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        observeViewModel()
        setOnClickListener()
        setupSwipeRefresh()
        viewModel.loadAuthorNews(args.author) // Загрузка новостей автора при старте
        (activity as MainActivity).setSelectedMenuItem(R.id.subscribedAuthorsFragment)
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshAuthorNews(args.author)
        }
    }

    // Настройка адаптера для RecyclerView
    private fun setupAdapter() {
        adapter = NewsListAdapter(requireActivity().application)
        binding.rcNews.adapter = adapter
        adapter.shouldHideRetryButton = true
        adapter.onNewsClickListener = { news ->
            launchNewsDetailsFragment(news)
        }
        binding.currentAuthor.text = args.author
    }

    // Подписка на наблюдение за состоянием UI и авторизацией
    private fun observeViewModel() {
        // Наблюдение за состоянием UI
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.uiState.collect { uiState ->
                        handleUiState(uiState)
                    }
                }

                launch {
                    viewModel.user.collect { authState ->
                        if (authState is AuthState.LoggedOut) {
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }
    }

    // Обработка текущего состояния UI
    private fun handleUiState(uiState: NewsState) {
        when (uiState) {
            is NewsState.Error -> {
                showToast()
                binding.pgNews.visibility = View.GONE
                binding.tvRetry.visibility = View.VISIBLE
                binding.swipeRefreshLayout.isRefreshing = false
            }

            is NewsState.Loading -> {
                binding.tvRetry.visibility = View.GONE
                binding.pgNews.visibility = View.VISIBLE
            }

            is NewsState.Success -> {
                binding.pgNews.visibility = View.GONE
                binding.tvRetry.visibility = View.GONE
                adapter.submitList(uiState.currentList)
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    // Установка обработчиков нажатий
    private fun setOnClickListener() {
        binding.tvRetry.setOnClickListener {
            binding.pgNews.visibility = View.VISIBLE
            binding.tvRetry.visibility = View.GONE
            viewModel.refreshAuthorNews((args.author))
        }
    }

    // Показ всплывающего уведомления с сообщением об ошибке
    private fun showToast() {    // Показ уведомления
        Toast.makeText(
            requireContext(),
            getString(R.string.error_load_data),
            Toast.LENGTH_LONG
        ).show()
    }

    // Переход к деталям новости
    private fun launchNewsDetailsFragment(news: NewsItemEntity) {
        viewModel.preloadAuthorData(args.author)
        findNavController().navigate(
            AuthorNewsFragmentDirections.actionAuthorNewsFragmentToNewsDetailsFragment3(
                news,
                args.author,
                R.id.subscribedAuthorsFragment
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}