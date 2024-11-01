package com.example.newswave.presentation.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.newswave.R
import com.example.newswave.app.NewsApp
import com.example.newswave.databinding.FragmentSettingsBinding
import com.example.newswave.presentation.MainActivity
import com.example.newswave.presentation.viewModels.SettingsViewModel
import com.example.newswave.presentation.viewModels.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject


class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var viewModel: SettingsViewModel

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
    ): View? {
        binding = FragmentSettingsBinding.inflate(layoutInflater)
        (activity as MainActivity).setSelectedMenuItem(R.id.settingsFragment)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory)[SettingsViewModel::class.java]
        observeViewModel()

        binding.btSignIn.setOnClickListener {
            launchSignInFragment()
        }

        binding.tvLogout.setOnClickListener {
            viewModel.logout()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.user.collect { firebaseUser ->
                    updateUI(firebaseUser != null)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED){
                viewModel.userData.collect{ user ->
                    if (user != null){
                        binding.tvName.text = "${user.firstName} ${user.lastName}"
                        binding.tvEmail.text = user.email
                        binding.tvUsername.text = user.username
                    }
                }
            }
        }
    }

    private fun updateUI(isUserLoggedIn: Boolean) {
        if (isUserLoggedIn) {
            binding.tvName.visibility = View.VISIBLE
            binding.btSignIn.visibility = View.GONE
            binding.tvEmail.visibility = View.VISIBLE
            binding.cvLogout.visibility = View.VISIBLE
            binding.tvUsername.visibility = View.VISIBLE
            binding.cvUserData.visibility = View.VISIBLE
        } else {
            binding.btSignIn.visibility = View.VISIBLE
            binding.cvLogout.visibility = View.GONE
            binding.tvName.visibility = View.GONE
            binding.cvUserData.visibility = View.GONE
            binding.tvEmail.visibility = View.GONE
        }
    }

    private fun launchSignInFragment() {
        findNavController().navigate(
            SettingsFragmentDirections.actionSettingsFragmentToLoginFragment()
        )
    }

}