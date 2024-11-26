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
import androidx.navigation.fragment.navArgs
import com.example.newswave.R
import com.example.newswave.app.NewsApp
import com.example.newswave.databinding.FragmentForgotPasswordBinding
import com.example.newswave.presentation.viewModels.ForgotPasswordViewModel
import com.example.newswave.presentation.viewModels.ViewModelFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class ForgotPasswordFragment : Fragment() {

    private lateinit var binding: FragmentForgotPasswordBinding
    private lateinit var viewModel: ForgotPasswordViewModel
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
        viewModel = ViewModelProvider(this, viewModelFactory)[ForgotPasswordViewModel::class.java]
        observeViewModel()
        binding.etEmail.setText(args.email)

        binding.btReset.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (isFieldNotEmpty(email)) {
                viewModel.resetPassword(email)
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.please_enter_your_email), Toast.LENGTH_LONG
                ).show()

            }
        }
    }

    private fun isFieldNotEmpty(email: String): Boolean {
        return email.isNotEmpty()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.isSuccess.collect { success ->
                    if (success) {
                        Toast.makeText(
                            requireContext(),
                            "The reset link has been successfully sent",
                            Toast.LENGTH_LONG
                        ).show()
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

}