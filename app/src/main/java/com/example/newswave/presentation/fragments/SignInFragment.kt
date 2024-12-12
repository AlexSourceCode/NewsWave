package com.example.newswave.presentation.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.newswave.R
import com.example.newswave.app.NewsApp
import com.example.newswave.databinding.FragmentSignInBinding
import com.example.newswave.presentation.MainActivity
import com.example.newswave.presentation.viewModels.SessionViewModel
import com.example.newswave.presentation.viewModels.SignInViewModel
import com.example.newswave.presentation.viewModels.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Фрагмент для авторизации пользователя
 */
class SignInFragment : Fragment() {

    private lateinit var binding: FragmentSignInBinding
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
        binding = FragmentSignInBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setupOnClickListener()

        // Получаем идентификатор активного элемента нижнего меню из аргументов
        // и обновляем состояние BottomNavigationView
        val activeItemId = args.currentBottomItem
        (activity as MainActivity).setSelectedMenuItem(activeItemId)
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
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.user.collect { fireBaseUser ->
                        if (fireBaseUser != null) {
                            findNavController().popBackStack()
                        }
                    }
                }

                launch {
                    viewModel.error.collect { errorMessage ->
                        showToast(errorMessage)
                    }
                }
            }
        }
    }

    // Переход на экран регистрации
    private fun launchRegistrationFragment() {
        findNavController().navigate(
            SignInFragmentDirections.actionLoginFragmentToRegistrationFragment()
        )
    }

    // Переход на экран восстановления пароля с передачей email
    private fun launchForgotPasswordFragment() {
        val email = binding.etEmail.text.toString().trim()
        findNavController().navigate(
            SignInFragmentDirections.actionLoginFragmentToForgotPasswordFragment(email)
        )
    }
}


//private fun observeViewModel() { // ?
//    lifecycleScope.launch {
//        repeatOnLifecycle(Lifecycle.State.CREATED) {
//            viewModel.user.collect { fireBaseUser ->
//                if (fireBaseUser != null) {
//                    findNavController().popBackStack()
//                }
//            }
//        }
//    }
//    lifecycleScope.launch {
//        repeatOnLifecycle(Lifecycle.State.STARTED) {
//            viewModel.error.collect { errorMessage ->
//                Log.d("CheckErrorState", "execute from SignInFragment")
//                showToast(errorMessage)
//            }
//        }
//    }
//}