package com.example.newswave.presentation.fragments

import android.content.Context
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import android.util.Patterns
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
import com.example.newswave.app.NewsApp
import com.example.newswave.databinding.FragmentRegistrationBinding
import com.example.newswave.presentation.MainActivity
import com.example.newswave.presentation.viewModels.RegistrationViewModel
import com.example.newswave.presentation.viewModels.ViewModelFactory
import com.example.newswave.utils.InputValidator
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * RegistrationFragment отвечает за регистрацию нового пользователя
 */
class RegistrationFragment : Fragment() {

    private lateinit var binding: FragmentRegistrationBinding
    private val viewModel: RegistrationViewModel by viewModels { viewModelFactory }
    private val args by navArgs<RegistrationFragmentArgs>()

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
    ): View {
        binding = FragmentRegistrationBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupOnClickListener()
        observeViewModel()
        (activity as MainActivity).setSelectedMenuItem(args.currentBottomItem)
    }

    // Подписка на изменения данных во ViewModel
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeSuccess() }
                launch { observeError() }
            }
        }
    }

    // Наблюдение за успешной авторизацией
    private suspend fun observeSuccess() {
        viewModel.user.collect { fireBaseUser ->
            if (fireBaseUser != null) {
                findNavController().popBackStack()
            }
        }
    }

    // Наблюдение за ошибками
    private suspend fun observeError() {
        viewModel.error.collect {
            showToast(it)
        }
    }

    // Настройка слушателей кнопок
    private fun setupOnClickListener() {
        binding.tvSignIn.setOnClickListener { // Переход на экран входа
            findNavController().popBackStack()
        }
        binding.btRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val firstName = binding.etFirstName.text.toString().trim()
            val lastName = binding.etLastName.text.toString().trim()

            if (InputValidator.validateRegistrationInput(
                    context = requireContext(),
                    username = username,
                    email = email,
                    password = password,
                    firstName = firstName,
                    lastName = lastName,
                    showToast = this::showToast
                )
            ) {
                viewModel.signUp(username, email, password, firstName, lastName)
            }
        }
    }

    // Отображение сообщения пользователю
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}