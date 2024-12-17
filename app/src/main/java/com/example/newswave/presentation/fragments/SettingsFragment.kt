package com.example.newswave.presentation.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.newswave.R
import com.example.newswave.app.NewsApp
import com.example.newswave.databinding.FragmentSettingsBinding
import com.example.newswave.presentation.activity.MainActivity
import com.example.newswave.presentation.viewModels.SettingsViewModel
import com.example.newswave.presentation.viewModels.ViewModelFactory
import com.example.newswave.presentation.model.LanguageOption
import com.example.newswave.utils.LanguageUtils
import com.example.newswave.utils.LocaleHelper
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Фрагмент настроек приложения, предоставляющий функционал управления настройками языка, профиля пользователя и т.д.
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels { viewModelFactory }
    private val languageMaps = LanguageUtils

    companion object {
        private const val KEY_SELECTION_RESULT = "selected_result"
        private const val INTERFACE_LANGUAGE_SELECTION_RESULT =
            "interface_language_selection_result"
        private const val CONTENT_LANGUAGE_SELECTION_RESULT = "content_language_selection_result"
        private const val SOURCE_COUNTRY_LANGUAGE_SELECTION_RESULT =
            "source_country_language_selection_result"
        private const val REFRESH_REQUEST_KEY = "refresh_request"
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setupClickListeners()
        updateInterfaceLanguageValue()
        (activity as MainActivity).setSelectedMenuItem(R.id.settingsFragment)
    }

    // Настройка слушателей для результатов выбора языка
    private fun setupLanguageResultListeners() {
        observeLanguageSelection(
            INTERFACE_LANGUAGE_SELECTION_RESULT,
            languageMaps.interfaceMap,
            viewModel::saveInterfaceLanguage
        )
        observeLanguageSelection(
            CONTENT_LANGUAGE_SELECTION_RESULT,
            languageMaps.contentLanguage,
            viewModel::saveContentLanguage
        )
        observeLanguageSelection(
            SOURCE_COUNTRY_LANGUAGE_SELECTION_RESULT,
            languageMaps.sourceCountry,
            viewModel::saveSourceCountry
        )
    }

    // Универсальная функция для наблюдения за результатами выбора языка
    private fun observeLanguageSelection(
        resultKey: String,
        map: Map<String, String>,
        saveLanguageAction: (String) -> Unit
    ) {
        parentFragmentManager.setFragmentResultListener(resultKey, this) { _, bundle ->
            bundle.getString(KEY_SELECTION_RESULT)?.let {
                saveLanguageAction(languageMaps.getLanguageCode(it, map))
                restartWithNewLocale()
            }
        }
    }

    // Настройка слушателей кликов
    private fun setupClickListeners() {
        setupLanguageResultListeners()

        binding.btSignIn.setOnClickListener { launchSignInFragment() }
        binding.tvChangePassword.setOnClickListener {
            val email = binding.tvEmailValue.text.toString()
            launchForgotPasswordFragment(email)
        }
        binding.tvLogout.setOnClickListener {
            viewModel.signOut()
        }
        binding.llContentLanguage.setOnClickListener {
            val languageName = binding.tvContentLanguageValue.text.toString()
            navigateToLanguageSelectionPopup(LanguageOption.CONTENT_LANGUAGE, languageName)
        }
        binding.llInterfaceLanguage.setOnClickListener {
            val languageCode = viewModel.getInterfaceLanguage()
            val languageName =
                LanguageUtils.getLanguageName(languageCode, LanguageUtils.interfaceMap)

            if (languageName == LocaleHelper.SYSTEM_DEFAULT)
                navigateToLanguageSelectionPopup(
                    LanguageOption.INTERFACE_LANGUAGE,
                    getString(R.string.system)
                )
            else
                navigateToLanguageSelectionPopup(LanguageOption.INTERFACE_LANGUAGE, languageName)
        }
        binding.llNewsSourceCountry.setOnClickListener {
            val languageName = binding.tvNewsSourceCountryValue.text.toString()
            navigateToLanguageSelectionPopup(LanguageOption.NEWS_SOURCE_COUNTRY, languageName)
        }
    }

    // Наблюдение за изменениями в ViewModel
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeUser() }
                launch { observeUserData() }
                launch { observeContentLanguage() }
                launch { observeSourceCountry()}
            }
        }
    }

    private suspend fun observeUser(){
        viewModel.user.collect { firebaseUser ->
            updateUI(firebaseUser != null)
        }
    }

    private suspend fun observeUserData(){
        viewModel.userData.collect { user ->
            if (user != null) {
                val fullName = "${user.firstName} ${user.lastName}"
                if (fullName.length > 20) {
                    binding.tvName.text = "${user.firstName}\n${user.lastName}"
                } else {
                    binding.tvName.text = fullName
                }
                binding.tvEmailValue.text = user.email
                binding.tvUsername.text = user.username
            }
        }
    }

    private suspend fun observeContentLanguage(){
        viewModel.contentLanguage.collect { languageCode ->
            binding.tvContentLanguageValue.text =
                LanguageUtils.getLanguageName(
                    languageCode,
                    languageMaps.contentLanguage
                )
        }
    }

    private suspend fun observeSourceCountry(){
        viewModel.sourceCountry.collect { countryCode ->
            binding.tvNewsSourceCountryValue.text =
                LanguageUtils.getLanguageName(countryCode, languageMaps.sourceCountry)
        }
    }

    // Переход к экрану выбора языка
    private fun navigateToLanguageSelectionPopup(option: LanguageOption, languageValue: String) {
        val action =
            SettingsFragmentDirections.actionSettingsFragmentToLanguageBottomSheetFragment2(
                option,
                languageValue
            )
        findNavController().navigate(action)
    }

    // Обновление UI на основе состояния пользователя
    private fun updateUI(isUserLoggedIn: Boolean) {
        binding.apply {
            if (isUserLoggedIn) {
                tvName.visibility = View.VISIBLE
                btSignIn.visibility = View.GONE
                tvEmail.visibility = View.VISIBLE
                cvLogout.visibility = View.VISIBLE
                tvUsername.visibility = View.VISIBLE
                cvUserData.visibility = View.VISIBLE
            } else {
                btSignIn.visibility = View.VISIBLE
                cvLogout.visibility = View.GONE
                tvName.visibility = View.GONE
                cvUserData.visibility = View.GONE
                tvUsername.visibility = View.GONE
            }
        }
    }

    // Обновление значения языка интерфейса
    private fun updateInterfaceLanguageValue() {
        binding.tvInterfaceLanguageValue.text =
            LanguageUtils.getLanguageName(
                viewModel.getInterfaceLanguage(),
                languageMaps.interfaceMap
            )
    }

    // Перезапуск приложения с новой локалью
    private fun restartWithNewLocale() {
        requireActivity().recreate()
        parentFragmentManager.setFragmentResult(REFRESH_REQUEST_KEY, Bundle.EMPTY)
    }

    // Метод для перехода к фрагменту входа в аккаунт
    private fun launchSignInFragment() {
        findNavController().navigate(
            SettingsFragmentDirections.actionSettingsFragmentToLoginFragment(R.id.settingsFragment)
        )
    }

    // Переход к фрагменту сброса пароля
    private fun launchForgotPasswordFragment(email: String) {
        findNavController().navigate(
            SettingsFragmentDirections.actionSettingsFragmentToForgotPasswordFragment(email, R.id.settingsFragment)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

