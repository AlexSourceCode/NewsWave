package com.example.newswave.presentation.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.newswave.R
import com.example.newswave.databinding.FragmentAuthorNewsBinding
import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.presentation.NewsApp
import com.example.newswave.presentation.adapters.NewsListAdapter
import com.example.newswave.presentation.viewModels.AuthorNewsViewModel
import com.example.newswave.presentation.viewModels.ViewModelFactory
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
    }


    private fun setupAdapter(){
        adapter = NewsListAdapter(requireActivity().application)
        binding.rcNews.adapter = adapter
        adapter.onNewsClickListener = { news ->
            launchNewsDetailsFragment(news)
        }
    }

    private fun observeViewModel(){
        viewModel.loadAuthorNews(args.author)
        viewModel.newsList.observe(viewLifecycleOwner){
            adapter.submitList(it)
        }
    }

    private fun launchNewsDetailsFragment(news: NewsItemEntity){
        findNavController().navigate(
            AuthorNewsFragmentDirections.actionAuthorNewsFragmentToNewsDetailsFragment3(news)
        )
    }


}