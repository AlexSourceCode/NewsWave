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
import com.example.newswave.R
import com.example.newswave.app.NewsApp
import com.example.newswave.databinding.FragmentRegistrationBinding
import com.example.newswave.presentation.viewModels.RegistrationViewModel
import com.example.newswave.presentation.viewModels.ViewModelFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class RegistrationFragment : Fragment() {

    private lateinit var binding: FragmentRegistrationBinding

    private lateinit var viewModel: RegistrationViewModel

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
        viewModel = ViewModelProvider(this, viewModelFactory)[RegistrationViewModel::class.java]
        setupOnClickListener()
        observeViewModel()

    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.user.collect { fireBaseUser ->
                    if (fireBaseUser != null) {
                        findNavController().popBackStack()
                    }
                }
            }
        }
//        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.CREATED) {
//                viewModel.error.collect()
//            }
//        }
    }

    private fun setupOnClickListener() {
        binding.tvSignIn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val firstName = binding.etFirstName.text.toString().trim()
            val lastName = binding.etLastName.text.toString().trim()


            if (validateInputFields(username, email, password, firstName, lastName)) {
                viewModel.signUp(username, email, password, firstName, lastName)
            }
        }
    }

    private fun validateInputFields(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Boolean {
        if (username.length < 3 || username.length > 20) {
            showToast(getString(R.string.invalid_username_length))
            return false
        }

        // Проверка формата email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast(getString(R.string.invalid_email_format))
            return false
        }

        if (password.isEmpty()) {
            showToast(getString(R.string.invalid_password_length))
            return false
        }

        // Проверка длины имени
        if (firstName.length < 2 || firstName.length > 20) {
            showToast(getString(R.string.invalid_first_name_length))
            return false
        }

        // Проверка длины фамилии
        if (lastName.length < 2 || lastName.length > 20) {
            showToast(getString(R.string.invalid_last_name_length))
            return false
        }

        return true
    }

    private fun showToast(message: String) {
        Log.d("CheckCalledCount", "flag showToast: ")
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }


}