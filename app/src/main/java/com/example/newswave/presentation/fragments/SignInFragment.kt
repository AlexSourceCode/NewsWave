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
import com.example.newswave.databinding.FragmentSignInBinding
import com.example.newswave.presentation.activity.MainActivity
import com.example.newswave.presentation.viewModels.SignInViewModel
import com.example.newswave.presentation.viewModels.ViewModelFactory
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Фрагмент для авторизации пользователя
 */
class SignInFragment : Fragment() {

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SignInViewModel by viewModels { viewModelFactory }
    private val args by navArgs<SignInFragmentArgs>()

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
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setupOnClickListener()

        // Получаем идентификатор активного элемента нижнего меню из аргументов
        // и обновляем состояние BottomNavigationView
        (activity as MainActivity).setSelectedMenuItem(args.currentBottomItem)
    }

    // Настройка обработчиков нажатий
    private fun setupOnClickListener() {
        binding.apply {
            tvRegistration.setOnClickListener { launchRegistrationFragment() }
            tvForgotPassword.setOnClickListener { launchForgotPasswordFragment() }
            btSignIn.setOnClickListener { handleSignIn() }
        }
    }

    // Обработка авторизации пользователя
    private fun handleSignIn() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        if (isFieldNotEmpty(email, password)) {
            viewModel.signIn(email, password)
        } else {
            showToast(getString(R.string.please_fill_in_all_the_fields))
        }
    }

    // Отображение всплывающего уведомления
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    // Проверка на заполненность полей
    private fun isFieldNotEmpty(email: String, password: String): Boolean {
        return email.isNotEmpty() && password.isNotEmpty()
    }

    // Подписка на изменения ViewModel
    private fun observeViewModel() { // ?
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeSuccess() }
                launch { observeError() }
            }
        }
    }

    private suspend fun observeSuccess() {
        viewModel.user.collect { fireBaseUser ->
            if (fireBaseUser != null) {
                findNavController().popBackStack()
            }
        }
    }

    private suspend fun observeError() {
        viewModel.error.collect { errorMessage ->
            showToast(errorMessage)
        }
    }

    // Переход на экран регистрации
    private fun launchRegistrationFragment() {
        findNavController().navigate(
            SignInFragmentDirections.actionLoginFragmentToRegistrationFragment(args.currentBottomItem)
        )
    }

    // Переход на экран восстановления пароля с передачей email
    private fun launchForgotPasswordFragment() {
        val email = binding.etEmail.text.toString().trim()
        findNavController().navigate(
            SignInFragmentDirections.actionLoginFragmentToForgotPasswordFragment(email, args.currentBottomItem)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}