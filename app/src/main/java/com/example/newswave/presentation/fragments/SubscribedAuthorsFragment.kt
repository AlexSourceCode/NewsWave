package com.example.newswave.presentation.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.newswave.R
import com.example.newswave.databinding.FragmentSubscribedAuthorsBinding
import com.example.newswave.app.NewsApp
import com.example.newswave.presentation.state.AuthState
import com.example.newswave.presentation.state.AuthorState
import com.example.newswave.presentation.MainActivity
import com.example.newswave.presentation.adapters.AuthorListAdapter
import com.example.newswave.presentation.viewModels.SubscribedAuthorsViewModel
import com.example.newswave.presentation.viewModels.ViewModelFactory
import kotlinx.coroutines.launch
import javax.inject.Inject


class SubscribedAuthorsFragment : Fragment() {

    private lateinit var binding: FragmentSubscribedAuthorsBinding
    private lateinit var adapter: AuthorListAdapter

    private val component by lazy {
        (requireActivity().application as NewsApp).component
    }
    private val viewModel: SubscribedAuthorsViewModel by viewModels { viewModelFactory }

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
        (activity as MainActivity).setSelectedMenuItem(R.id.subscribedAuthorsFragment)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        observeViewModel()

        binding.btLogin.setOnClickListener {
            launchLoginFragment()
        }
        binding.tvRetry.setOnClickListener {
            binding.tvRetry.visibility = View.GONE
            binding.pgNews.visibility = View.VISIBLE
            viewModel.retryFetchAuthors()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.user.collect { firebaseUser ->
                    when (firebaseUser) {
                        is AuthState.LoggedIn -> {
                            showProgressBar()
                            showLoggedInState()
                            viewModel.uiState.collect { uiState ->
                                when (uiState) {
                                    is AuthorState.Error -> {
                                        showToast()
                                        hideProgressBar()
                                        binding.tvRetry.visibility = View.VISIBLE
                                    }

                                    is AuthorState.Loading -> {
                                        binding.tvRetry.visibility = View.GONE
                                        showProgressBar()
                                    }

                                    is AuthorState.Success -> {
                                        binding.tvRetry.visibility = View.GONE
                                        if (uiState.currentList?.size == 0){
                                            showMessageNoAuthors()
                                            hideProgressBar()
                                        } else {
                                            adapter.submitList(uiState.currentList)
                                            hideMessageNoAuthors()
                                            hideProgressBar()
                                        }
                                    }
                                }
                            }
                        }
                        is AuthState.LoggedOut -> showLoggedOutState()
                    }
                }
            }
        }
    }

    private fun showToast() {    // Показ уведомления
        Toast.makeText(
            requireContext(),
            requireActivity().getString(R.string.error_load_data),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showMessageNoAuthors(){
        binding.tvNoAuthors.visibility = View.VISIBLE
    }
    private fun hideMessageNoAuthors(){
        binding.tvNoAuthors.visibility = View.GONE
    }

    private fun showLoggedOutState() {
        binding.textContainer.visibility = View.VISIBLE
        binding.rcAuthors.visibility = View.GONE
    }

    private fun showLoggedInState() {
        binding.rcAuthors.visibility = View.VISIBLE
        binding.textContainer.visibility = View.GONE
    }


    private fun showProgressBar() {
        binding.pgNews.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.pgNews.visibility = View.GONE
    }


    private fun setupAdapter() {
        adapter = AuthorListAdapter()
        binding.rcAuthors.adapter = adapter
        adapter.onAuthorClickSubscription = { author ->
            showUnsubscribeDialog(author)
        }
        adapter.onAuthorClickNews = { author ->
            launchAuthorNewsFragment(author)
        }
    }

    private fun showUnsubscribeDialog(author: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.confirmation))
        builder.setMessage(getString(R.string.alert_dialog_question, author))

        builder.setPositiveButton(getString(R.string.positive_answer)) { dialog, _ ->
            viewModel.unsubscribeFromAuthor(author)
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.negative_answer)) { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun launchAuthorNewsFragment(author: String) {
        findNavController().navigate(
            SubscribedAuthorsFragmentDirections.actionSubscribedAuthorsFragmentToAuthorNewsFragment(
                author
            )
        )
    }

    private fun launchLoginFragment() {
        findNavController().navigate(
            SubscribedAuthorsFragmentDirections.actionSubscribedAuthorsFragmentToLoginFragment(R.id.subscribedAuthorsFragment)
        )
    }

}