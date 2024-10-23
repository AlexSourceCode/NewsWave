package com.example.newswave.presentation.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.newswave.app.NewsApp
import com.example.newswave.databinding.FragmentSettingsBinding
import com.example.newswave.presentation.viewModels.SettingsViewModel
import com.example.newswave.presentation.viewModels.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import javax.inject.Inject


class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var viewModel: SettingsViewModel
    private lateinit var auth: FirebaseAuth

    @Inject
    lateinit var viewmodel: ViewModelFactory

    private val component by lazy {
        (requireActivity().application as NewsApp).component
    }

    override fun onAttach(context: Context) {
        component.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        auth = Firebase.auth
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkAuthState()

        binding.btSignIn.setOnClickListener {
            launchSignInFragment()
        }
    }

    private fun checkAuthState(){
        val currentUser = auth.currentUser
        if (currentUser == null){
            binding.btSignIn.visibility = View.VISIBLE
        } else{
            binding.tvName.visibility = View.VISIBLE // сделать нормальное присваивание
            binding.tvUsername.visibility = View.VISIBLE
        }
    }

    private fun launchSignInFragment(){
        findNavController().navigate(
            SettingsFragmentDirections.actionSettingsFragmentToLoginFragment()
        )
    }

}