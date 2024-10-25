package com.example.newswave.presentation.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.newswave.R
import com.example.newswave.databinding.FragmentSubscribedAuthorsBinding
import com.example.newswave.app.NewsApp
import com.example.newswave.domain.model.AuthorState
import com.example.newswave.presentation.adapters.AuthorListAdapter
import com.example.newswave.presentation.viewModels.SubscribedAuthorsViewModel
import com.example.newswave.presentation.viewModels.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


class SubscribedAuthorsFragment : Fragment() {

    private lateinit var binding: FragmentSubscribedAuthorsBinding
    private lateinit var adapter: AuthorListAdapter

    private val component by lazy {
        (requireActivity().application as NewsApp).component
    }
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
        viewModel =
            ViewModelProvider(this, viewModelFactory)[SubscribedAuthorsViewModel::class.java]
        setupAdapter()
        observeViewModel()

        binding.btLogin.setOnClickListener {
            launchLoginFragment()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.uiState.collect { uiState ->
                    when (uiState) {
                        is AuthorState.Error -> Log.d(
                            "CheckState",
                            uiState.toString()
                        ) // When will be repository then need to change handle error.
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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.user.collect { firebaseUser ->
                        if (firebaseUser == null) {
                            binding.textContainer.visibility = View.VISIBLE
                            binding.rcAuthors.visibility = View.GONE
                        } else {
                            binding.rcAuthors.visibility = View.VISIBLE
                            binding.textContainer.visibility = View.GONE
                        }
                }
            }
        }
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
            SubscribedAuthorsFragmentDirections.actionSubscribedAuthorsFragmentToLoginFragment()
        )
    }

}