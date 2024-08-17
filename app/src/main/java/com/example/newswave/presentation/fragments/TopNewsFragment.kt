package com.example.newswave.presentation.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.newswave.databinding.FragmentTopNewsBinding
import com.example.newswave.presentation.adapters.NewsListAdapter
import com.example.newswave.presentation.viewModels.TopNewsViewModel
import kotlinx.coroutines.flow.map


class TopNewsFragment : Fragment() {

    private lateinit var binding: FragmentTopNewsBinding
    private lateinit var adapter: NewsListAdapter
    private val viewModel by lazy {
        ViewModelProvider(this)[TopNewsViewModel :: class.java]
    }

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

    }

    private fun observeViewModel(){
        viewModel.newsList.observe(viewLifecycleOwner){
            adapter.submitList(it)
        }

    }

    private fun setupAdapter(){
        adapter = NewsListAdapter(requireActivity().application)
        binding.rcNews.adapter = adapter
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TopNewsFragment()
    }
}