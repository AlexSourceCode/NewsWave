package com.example.newswave.presentation.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.newswave.databinding.FragmentSubscribedAuthorsBinding
import com.example.newswave.app.NewsApp
import com.example.newswave.domain.model.AuthorState
import com.example.newswave.presentation.adapters.AuthorListAdapter
import com.example.newswave.presentation.viewModels.SubscribedAuthorsViewModel
import com.example.newswave.presentation.viewModels.ViewModelFactory
import kotlinx.coroutines.launch
import javax.inject.Inject


class SubscribedAuthorsFragment : Fragment() {

    private lateinit var binding: FragmentSubscribedAuthorsBinding

    private val component by lazy {
        (requireActivity().application as NewsApp).component
    }

    private lateinit var adapter: AuthorListAdapter

    private lateinit var viewModel: SubscribedAuthorsViewModel
    @Inject
    lateinit var viewModelFactory: ViewModelFactory



    override fun onAttach(context: Context) {
        component.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSubscribedAuthorsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory)[SubscribedAuthorsViewModel::class.java]
        setupAdapter()
        observeViewModel()
    }

    private fun setupAdapter(){
        adapter = AuthorListAdapter()
        binding.rcAuthors.adapter = adapter
        adapter.onAuthorClickSubscription = { author ->
            viewModel.unsubscribeFromAuthor(author)
        }
        adapter.onAuthorClickNews = { author ->
            launchAuthorNewsFragment(author)
        }
    }

    private fun observeViewModel(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED){
                viewModel.uiState.collect{ uiState ->
                    Log.d("CheckState", uiState.toString())
                    when(uiState){
                        is AuthorState.Error -> Log.d("CheckState", uiState.toString())
                        is AuthorState.Loading -> {
                            binding.pgNews.visibility = View.VISIBLE
                        }
                        is AuthorState.Success -> {
                            binding.pgNews.visibility = View.GONE
                            adapter.submitList(uiState.currentList)
                        }
                    }
                }
            }
        }

    }

    private fun launchAuthorNewsFragment(author: String){
        findNavController().navigate(
            SubscribedAuthorsFragmentDirections.actionSubscribedAuthorsFragmentToAuthorNewsFragment(author)
        )
    }

}