package com.example.newswave.presentation.fragments

import android.content.Context
import androidx.fragment.app.viewModels
import android.os.Bundle
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
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.error.collect { errorMessage ->
                    Toast.makeText(requireActivity().application, errorMessage, Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
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

            if (isFieldNotEmpty(username, email, password, firstName, lastName)){
                viewModel.signUp(username, email, password, firstName, lastName)
            } else{
                Toast.makeText(
                    requireContext(),
                    getString(R.string.please_fill_in_all_the_fields), Toast.LENGTH_LONG
                )
                    .show()
            }
        }
    }

    private fun isFieldNotEmpty(
        username: String,
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Boolean {
        return email.isNotEmpty() &&
                username.isNotEmpty() &&
                password.isNotEmpty() &&
                firstName.isNotEmpty() &&
                lastName.isNotEmpty()
    }


}