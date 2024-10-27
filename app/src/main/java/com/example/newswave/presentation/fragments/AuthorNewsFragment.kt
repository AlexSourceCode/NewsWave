package com.example.newswave.presentation.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.newswave.R
import com.example.newswave.databinding.FragmentAuthorNewsBinding
import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.app.NewsApp
import com.example.newswave.domain.model.AuthState
import com.example.newswave.domain.model.NewsState
import com.example.newswave.presentation.adapters.NewsListAdapter
import com.example.newswave.presentation.viewModels.AuthorNewsViewModel
import com.example.newswave.presentation.viewModels.ViewModelFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject


class AuthorNewsFragment : Fragment() {

    private lateinit var binding: FragmentAuthorNewsBinding
    private val args by navArgs<AuthorNewsFragmentArgs>()

    private lateinit var adapter: NewsListAdapter
    private lateinit var viewModel: AuthorNewsViewModel

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
        binding = FragmentAuthorNewsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory)[AuthorNewsViewModel::class.java]
        setupAdapter()
        observeViewModel()
        setOnClickListener()
        setupSwipeRefresh()


    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshData(args.author)
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }


    private fun setupAdapter() {
        adapter = NewsListAdapter(requireActivity().application)
        binding.rcNews.adapter = adapter
        adapter.shouldHideRetryButton = true
        adapter.onNewsClickListener = { news ->
            launchNewsDetailsFragment(news)
        }
        binding.currentAuthor.text = args.author
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.loadAuthorNews(args.author)
                viewModel.uiState.collect { uiState ->
                    when (uiState) {
                        is NewsState.Error -> {
                            showToast()
                            binding.pgNews.visibility = View.GONE
                            binding.tvRetry.visibility = View.VISIBLE
                        }
                        is NewsState.Loading -> {
                            binding.tvRetry.visibility = View.GONE
                            binding.pgNews.visibility = View.VISIBLE
                        }
                        is NewsState.Success -> {
                            binding.pgNews.visibility = View.GONE
                            binding.tvRetry.visibility = View.GONE
                            adapter.submitList(uiState.currentList)
                        }
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED){
                viewModel.user.collect{ authState ->
                    if (authState is AuthState.LoggedOut) findNavController().popBackStack()
                }
            }
        }
    }


    private fun setOnClickListener() {
        binding.tvRetry.setOnClickListener {
            viewModel.loadAuthorNews((args.author))
        }
    }

    private fun showToast(){    // Показ уведомления
        Toast.makeText(requireContext(), requireActivity().getString(R.string.error_load_data), Toast.LENGTH_LONG).show()
    }

    private fun launchNewsDetailsFragment(news: NewsItemEntity) {
        viewModel.preloadAuthorData(args.author)
        findNavController().navigate(
            AuthorNewsFragmentDirections.actionAuthorNewsFragmentToNewsDetailsFragment3(
                news,
                args.author
            )
        )
    }


}