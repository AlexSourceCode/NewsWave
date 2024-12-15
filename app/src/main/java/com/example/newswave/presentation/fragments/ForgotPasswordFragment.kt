package com.example.newswave.presentation.fragments

import android.content.Context
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.newswave.R
import com.example.newswave.app.NewsApp
import com.example.newswave.databinding.FragmentForgotPasswordBinding
import com.example.newswave.presentation.MainActivity
import com.example.newswave.presentation.viewModels.ForgotPasswordViewModel
import com.example.newswave.presentation.viewModels.SessionViewModel
import com.example.newswave.presentation.viewModels.ViewModelFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Fragment ForgotPasswordFragment отвечает за предоставление пользователю возможности сбросить пароль
 */
class ForgotPasswordFragment : Fragment() {

    private lateinit var binding: FragmentForgotPasswordBinding
    private val forgotPasswordViewModel: ForgotPasswordViewModel by viewModels { viewModelFactory }
    private val args by navArgs<ForgotPasswordFragmentArgs>()

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
        binding = FragmentForgotPasswordBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
        (activity as MainActivity).setSelectedMenuItem(args.currentBottomItem)
    }

    // Настройка пользовательского интерфейса
    private fun setupUI() {
        // Предзаполнение поля email, если оно было передано через навигационные аргументы
        binding.etEmail.setText(args.email)

        // Установка слушателя для кнопки сброса
        binding.btReset.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isNotEmpty()) {
                forgotPasswordViewModel.resetPassword(email)
            } else {
                showToast(getString(R.string.please_enter_your_email))
            }
        }
    }

    // Подписка на изменения данных в ViewModel.
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeSuccess() }
                launch { observeError() }
            }
        }
    }

    // Подписка на поток успешного выполнения операции
    private suspend fun observeSuccess(){
        forgotPasswordViewModel.isSuccess.collect { success ->
            if (success) {
                showToast(getString(R.string.reset_link_sent_successfully))
                findNavController().popBackStack()
            }
        }
    }

    // Подписка на поток ошибок
    private suspend fun observeError(){
        forgotPasswordViewModel.error.collect {
            showToast(it)
        }
    }

    // Метод для отображения сообщений Toast
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}