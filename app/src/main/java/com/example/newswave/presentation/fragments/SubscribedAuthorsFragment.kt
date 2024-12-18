package com.example.newswave.presentation.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.newswave.R
import com.example.newswave.app.NewsApp
import com.example.newswave.databinding.FragmentSubscribedAuthorsBinding
import com.example.newswave.presentation.activity.MainActivity
import com.example.newswave.presentation.adapters.AuthorListAdapter
import com.example.newswave.presentation.states.AuthState
import com.example.newswave.presentation.states.AuthorState
import com.example.newswave.presentation.viewModels.SubscribedAuthorsViewModel
import com.example.newswave.presentation.viewModels.ViewModelFactory
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Фрагмент для отображения списка авторов, на которых подписан пользователь
 */
class SubscribedAuthorsFragment : Fragment() {

    private var _binding: FragmentSubscribedAuthorsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AuthorListAdapter
    private val viewModel: SubscribedAuthorsViewModel by viewModels { viewModelFactory }

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
        _binding = FragmentSubscribedAuthorsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        setupClickListeners()
        observeViewModel()
        (activity as MainActivity).setSelectedMenuItem(R.id.subscribedAuthorsFragment)
    }

    // Настройка адаптера для списка авторов
    private fun setupAdapter() {
        adapter = AuthorListAdapter().apply {
            onAuthorClickSubscription = { showUnsubscribeDialog(it) }
            onAuthorClickNews = { launchAuthorNewsFragment(it) }
        }
        binding.rcAuthors.adapter = adapter
    }

    // Установка обработчиков нажатий для кнопок
    private fun setupClickListeners() {
        binding.btLogin.setOnClickListener { launchLoginFragment() }
        binding.tvRetry.setOnClickListener {
            binding.tvRetry.visibility = View.GONE
            binding.pgNews.visibility = View.VISIBLE
            viewModel.retryFetchAuthors()
        }
    }

    // Наблюдение за состоянием ViewModel
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.user.collect { firebaseUser ->
                    when (firebaseUser) {
                        is AuthState.LoggedIn -> { handleLoggedInState() }
                        is AuthState.LoggedOut -> showLoggedOutState()
                    }
                }
            }
        }
    }

    // Обработка состояния успешной авторизации
    private suspend fun handleLoggedInState() {
        showProgressBar()
        showLoggedInState()
        observeAuthorsState()
    }

    // Наблюдение за загрухкой списка авторов
    private suspend fun observeAuthorsState() {
        viewModel.uiState.collect { uiState ->
            when (uiState) {
                is AuthorState.Error -> showErrorState()
                is AuthorState.Loading -> showLoadingState()
                is AuthorState.Success -> handleSuccessState(uiState)
            }
        }
    }

    // Показ состояния ошибки
    private fun showErrorState() {
        showToast()
        hideProgressBar()
        binding.tvRetry.visibility = View.VISIBLE
    }

    // Показ состояния загрузки
    private fun showLoadingState() {
        binding.tvRetry.visibility = View.GONE
        showProgressBar()
    }

    // Обработка успешной загрузки
    private fun handleSuccessState(uiState: AuthorState.Success) {
        binding.tvRetry.visibility = View.GONE
        if (uiState.currentList.isNullOrEmpty()) {
            showMessageNoAuthors()
        } else {
            adapter.submitList(uiState.currentList)
            hideMessageNoAuthors()
        }
        hideProgressBar()
    }

    // Показ уведомления с ошибкой
    private fun showToast() {
        Toast.makeText(
            requireContext(),
            getString(R.string.error_load_data),
            Toast.LENGTH_SHORT
        ).show()
    }

    // Показ сообщения о том, что список авторов пуст
    private fun showMessageNoAuthors() {
        binding.tvNoAuthors.visibility = View.VISIBLE
    }

    // Скрытие сообщения о том, что список авторов пуст
    private fun hideMessageNoAuthors() {
        binding.tvNoAuthors.visibility = View.GONE
    }

    // Показ состояния, когда пользователь разлогинился
    private fun showLoggedOutState() {
        binding.textContainer.visibility = View.VISIBLE
        binding.rcAuthors.visibility = View.GONE
    }

    // Показ состояния, когда пользователь авторизован
    private fun showLoggedInState() {
        binding.rcAuthors.visibility = View.VISIBLE
        binding.textContainer.visibility = View.GONE
    }

    // Показ прогресс-бара
    private fun showProgressBar() {
        binding.pgNews.visibility = View.VISIBLE
    }

    // Скрытие прогресс-бара
    private fun hideProgressBar() {
        binding.pgNews.visibility = View.GONE
    }

    // Показ окна подтверждения отписки от автора
    private fun showUnsubscribeDialog(author: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirmation))
            .setMessage(getString(R.string.alert_dialog_question, author))
            .setPositiveButton(getString(R.string.positive_answer)) { dialog, _ ->
                viewModel.unsubscribeFromAuthor(author)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.negative_answer)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    // Переход к фрагменту новостей автора
    private fun launchAuthorNewsFragment(author: String) {
        findNavController().navigate(
            SubscribedAuthorsFragmentDirections.actionSubscribedAuthorsFragmentToAuthorNewsFragment(
                author,
                R.id.subscribedAuthorsFragment
            )
        )
    }

    // Переход к фрагменту логина
    private fun launchLoginFragment() {
        findNavController().navigate(
            SubscribedAuthorsFragmentDirections.actionSubscribedAuthorsFragmentToLoginFragment(R.id.subscribedAuthorsFragment)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}