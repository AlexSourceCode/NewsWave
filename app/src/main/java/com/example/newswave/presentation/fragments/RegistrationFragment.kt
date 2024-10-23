package com.example.newswave.presentation.fragments

import android.content.Context
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.newswave.app.NewsApp
import com.example.newswave.databinding.FragmentRegistrationBinding
import com.example.newswave.presentation.viewModels.RegistrationViewModel
import com.example.newswave.presentation.viewModels.ViewModelFactory
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        binding.tvSignIn.setOnClickListener {
            launchRegistrationFragment()
        }
    }

    private fun launchRegistrationFragment(){
        findNavController().navigate(
            RegistrationFragmentDirections.actionRegistrationFragmentToLoginFragment()
        )
    }
}