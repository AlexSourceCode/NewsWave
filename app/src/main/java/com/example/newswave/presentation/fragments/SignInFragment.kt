package com.example.newswave.presentation.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.newswave.R
import com.example.newswave.app.NewsApp
import com.example.newswave.databinding.FragmentSignInBinding
import com.example.newswave.presentation.viewModels.SignInViewModel
import com.example.newswave.presentation.viewModels.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class SignInFragment : Fragment() {

    private lateinit var binding: FragmentSignInBinding
    private lateinit var viewModel: SignInViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val component by lazy {
        (requireActivity().application as NewsApp).component
    }

    override fun onAttach(context: Context) {
        component.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        viewModel = ViewModelProvider(this, viewModelFactory)[SignInViewModel::class.java]
        observeViewModel()
        setupOnClickListener()
    }

    private fun setupOnClickListener() {
        binding.tvRegistration.setOnClickListener {
            launchRegistrationFragment()
        }
        binding.tvForgotPassword.setOnClickListener {
            launchForgotPasswordFragment()
        }

        binding.btSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.signIn(email, password)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.user.collect{ fireBaseUser ->
                    if (fireBaseUser != null){
                        findNavController().popBackStack()
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.error.collect{ errorMessage->
                    Toast.makeText(requireActivity().application, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun launchRegistrationFragment() {
        findNavController().navigate(
            SignInFragmentDirections.actionLoginFragmentToRegistrationFragment()
        )
    }

    private fun launchForgotPasswordFragment() {
        val email = binding.etEmail.text.toString().trim()
        findNavController().navigate(
            SignInFragmentDirections.actionLoginFragmentToForgotPasswordFragment(email)
        )
    }

}