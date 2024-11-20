package com.example.newswave.presentation.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.newswave.R
import com.example.newswave.app.NewsApp
import com.example.newswave.databinding.FragmentSettingsBinding
import com.example.newswave.presentation.MainActivity
import com.example.newswave.presentation.viewModels.SettingsViewModel
import com.example.newswave.presentation.viewModels.ViewModelFactory
import com.example.newswave.utils.LanguageOption
import com.example.newswave.utils.LocaleHelper
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject


class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var viewModel: SettingsViewModel

    private val languageMap by lazy {
        mapOf(
            getString(R.string.system) to "system", // Устройство по умолчанию
            "Русский" to "ru", // Русский
            "English" to "en"  // Английский
        )
    }

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
    ): View? {
        binding = FragmentSettingsBinding.inflate(layoutInflater)
        (activity as MainActivity).setSelectedMenuItem(R.id.settingsFragment)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory)[SettingsViewModel::class.java]
        observeViewModel()
        setupOnClickListener()

        parentFragmentManager.setFragmentResultListener(
            "language_selection_result",
            this
        ) { _, bundle ->
            val selectedLanguage = bundle.getString("selected_language")
            selectedLanguage?.let {
                Log.d("SettingsFragmentGetLanguage", "selectedLanguage $selectedLanguage")
                val languageCode = getLanguageCode(it) // тут в верхнем регистре значение
                Log.d("SettingsFragmentGetLanguage", "languageCode $languageCode")
                handleSelectedLanguage(languageCode)
                applyLocaleChanges()
            }
        }
    }

    private fun getLanguageCode(selectedLanguage: String): String { // получение из полного названия языка в languagecode
        return languageMap[selectedLanguage] ?: "system"
    }

    private fun getLanguageName(languageCode: String): String { // получение из languagecode в полного названия языка
        Log.d(
            "SettingsFragment",
            "getLanguageNameResult ${languageMap.entries.find { it.value == languageCode }?.key}"
        )
        return languageMap.entries.find { it.value == languageCode }?.key
            ?: "system" // Значение по умолчанию
    }

    private fun handleSelectedLanguage(language: String) {
        // Логика обработки выбранного языка
        // добавить textview binding.tvInterfaceLanguage.text = language
        viewModel.setInterfaceLanguage(language)
//        LocaleHelper.setLocale(requireContext(), language)
    }

    private fun setupOnClickListener() {
        binding.btSignIn.setOnClickListener {
            launchSignInFragment()
        }

        binding.tvLogout.setOnClickListener {
            viewModel.logout()
        }


//        binding.tvContentLanguage.setOnClickListener {
//            showLanguagePopup(LanguageOption.CONTENT_LANGUAGE)
//        }
        binding.tvInterfaceLanguage.setOnClickListener {
            val languageCode = viewModel.getInterfaceLanguage()
            Log.d("SettingsFragment", "languageCodeTradeToBottomSheet $languageCode")
            val languageName = getLanguageName(languageCode)
            Log.d("SettingsFragment", "TradeToBottomSheetLanguagenAME $languageName")
            if (languageName == "system")
                showLanguagePopup(LanguageOption.INTERFACE_LANGUAGE, requireContext().getString(R.string.system))
             else
                showLanguagePopup(LanguageOption.INTERFACE_LANGUAGE, languageName)
        }
//        binding.tvNewsSourceCountry.setOnClickListener {
//            showLanguagePopup(LanguageOption.NEWS_SOURCE_COUNTRY)
//        }
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
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.userData.collect { user ->
                    if (user != null) {
                        binding.tvName.text = "${user.firstName} ${user.lastName}"
                        binding.tvEmail.text = user.email
                        binding.tvUsername.text = user.username
                    }
                }
            }
        }
    }


    private fun showLanguagePopup(option: LanguageOption, languageValue: String) {
        val action =
            SettingsFragmentDirections.actionSettingsFragmentToLanguageBottomSheetFragment2(
                option,
                languageValue
            )
        findNavController().navigate(action)
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
            binding.tvUsername.visibility = View.GONE
        }
    }

    private fun applyLocaleChanges() {
        requireActivity().recreate() // Перезапускает Activity, сохраняя состояние фрагментов.
    }

    private fun launchSignInFragment() {
        findNavController().navigate(
            SettingsFragmentDirections.actionSettingsFragmentToLoginFragment()
        )
    }

}

