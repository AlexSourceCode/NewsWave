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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject


class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var viewModel: SettingsViewModel

    private val languageInterfaceMap by lazy {
        mapOf(
            getString(R.string.system) to LocaleHelper.SYSTEM_DEFAULT, // Устройство по умолчанию
            "Русский" to "ru", // Русский
            "English" to "en"  // Английский
        )
    }

    private val languageContentLanguage by lazy {
        mapOf(
            "Afar" to "aa",
            "Amharic" to "am",
            "Arabic" to "ar",
            "Bengali" to "bn",
            "Bosnian" to "bs",
            "Bulgarian" to "bg",
            "Burmese" to "my",
            "Chinese" to "zh",
            "Croatian" to "hr",
            "Czech" to "cs",
            "Danish" to "da",
            "Dutch" to "nl",
            "English" to "en",
            "Estonian" to "et",
            "Finnish" to "fi",
            "French" to "fr",
            "German" to "de",
            "Greek" to "el",
            "Hebrew" to "he",
            "Hindi" to "hi",
            "Hungarian" to "hu",
            "Icelandic" to "is",
            "Indonesian" to "id",
            "Italian" to "it",
            "Japanese" to "ja",
            "Korean" to "ko",
            "Lao" to "lo",
            "Latvian" to "lv",
            "Lithuanian" to "lt",
            "Macedonian" to "mk",
            "Malay" to "ms",
            "Maltese" to "mt",
            "Marathi" to "mr",
            "Māori" to "mi",
            "Nepali" to "ne",
            "Norwegian" to "nb",
            "Norwegian" to "no",
            "Persian" to "fa",
            "Polish" to "pl",
            "Portuguese" to "pt",
            "Romanian" to "ro",
            "Russian" to "ru",
            "Serbian" to "sr",
            "Sinhalese" to "si",
            "Slovak" to "sk",
            "Slovene" to "sl",
            "Somali" to "so",
            "Spanish" to "es",
            "Swedish" to "sv",
            "Tajik" to "tg",
            "Tamil" to "ta",
            "Telugu" to "te",
            "Thai" to "th",
            "Turkish" to "tr",
            "Ukrainian" to "uk",
            "Urdu" to "ur",
            "Uzbek" to "uz",
            "Vietnamese" to "vi"
        )
    }

    private val languageSourceCountry by lazy {
        mapOf(
            "Afghanistan" to "af",
            "Albania" to "al",
            "Algeria" to "dz",
            "American Samoa" to "as",
            "Anguilla" to "ai",
            "Argentina" to "ar",
            "Australia" to "au",
            "Austria" to "at",
            "Bahamas" to "bs",
            "Bahrain" to "bh",
            "Bangladesh" to "bd",
            "Barbados" to "bb",
            "Belarus" to "by",
            "Belgium" to "be",
            "Belize" to "bz",
            "Benin" to "bj",
            "Bermuda" to "bm",
            "Bhutan" to "bt",
            "Bosnia And Herzegovina" to "ba",
            "Bouvet Island" to "bv",
            "Brazil" to "br",
            "British Indian Ocean Territory" to "io",
            "Brunei Darussalam" to "bn",
            "Bulgaria" to "bg",
            "Burkina Faso" to "bf",
            "Burundi" to "bi",
            "Cambodia" to "kh",
            "Cameroon" to "cm",
            "Canada" to "ca",
            "Cape Verde" to "cv",
            "Central African Republic" to "cf",
            "Chile" to "cl",
            "China" to "cn",
            "Colombia" to "co",
            "Comoros" to "km",
            "Congo, Democratic Republic Of The" to "cd",
            "Congo, Republic Of The" to "cg",
            "Costa Rica" to "cr",
            "Cote D'ivoire" to "ci",
            "Croatia" to "hr",
            "Cuba" to "cu",
            "Cyprus" to "cy",
            "Czech Republic" to "cz",
            "Denmark" to "dk",
            "Djibouti" to "dj",
            "Dominican Republic" to "do",
            "Ecuador" to "ec",
            "Egypt" to "eg",
            "El Salvador" to "sv",
            "Eritrea" to "er",
            "Ethiopia" to "et",
            "Fiji" to "fj",
            "Finland" to "fi",
            "France" to "fr",
            "French Guiana" to "gf",
            "French Polynesia" to "pf",
            "Gambia" to "gm",
            "Germany" to "de",
            "Ghana" to "gh",
            "Greece" to "gr",
            "Guatemala" to "gt",
            "Guinea" to "gn",
            "Guyana" to "gy",
            "Honduras" to "hn",
            "Hong Kong" to "hk",
            "Hungary" to "hu",
            "Iceland" to "is",
            "India" to "in",
            "Indonesia" to "id",
            "Iran" to "ir",
            "Iraq" to "iq",
            "Ireland" to "ie",
            "Israel" to "il",
            "Italy" to "it",
            "Japan" to "jp",
            "Jordan" to "jo",
            "Kazakhstan" to "kz",
            "Kenya" to "ke",
            "Kuwait" to "kw",
            "Laos" to "la",
            "Latvia" to "lv",
            "Lebanon" to "lb",
            "Lesotho" to "ls",
            "Liberia" to "lr",
            "Liechtenstein" to "li",
            "Lithuania" to "lt",
            "Luxembourg" to "lu",
            "Macedonia" to "mk",
            "Madagascar" to "mg",
            "Malawi" to "mw",
            "Malaysia" to "my",
            "Maldives" to "mv",
            "Mali" to "ml",
            "Malta" to "mt",
            "Mauritania" to "mr",
            "Mauritius" to "mu",
            "Mexico" to "mx",
            "Micronesia, Federated States Of" to "fm",
            "Moldova" to "md",
            "Monaco" to "mc",
            "Montenegro" to "me",
            "Montserrat" to "ms",
            "Morocco" to "ma",
            "Myanmar" to "mm",
            "Namibia" to "na",
            "Nepal" to "np",
            "Netherlands" to "nl",
            "New Zealand" to "nz",
            "Niger" to "ne",
            "Nigeria" to "ng",
            "North Korea" to "kp",
            "Norway" to "no",
            "Oman" to "om",
            "Pakistan" to "pk",
            "Palestinian Territory" to "ps",
            "Paraguay" to "py",
            "Peru" to "pe",
            "Philippines" to "ph",
            "Poland" to "pl",
            "Portugal" to "pt",
            "Qatar" to "qa",
            "Romania" to "ro",
            "Russia" to "ru",
            "Rwanda" to "rw",
            "Saint Kitts And Nevis" to "kn",
            "Saint Vincent And The Grenadines" to "vc",
            "San Marino" to "sm",
            "Saudi Arabia" to "sa",
            "Senegal" to "sn",
            "Serbia" to "rs",
            "Seychelles" to "sc",
            "Sierra Leone" to "sl",
            "Singapore" to "sg",
            "Slovakia" to "sk",
            "Slovenia" to "si",
            "Solomon Islands" to "sb",
            "Somalia" to "so",
            "South Africa" to "za",
            "South Korea" to "kr",
            "Spain" to "es",
            "Sri Lanka" to "lk",
            "Suriname" to "sr",
            "Swaziland" to "sz",
            "Sweden" to "se",
            "Switzerland" to "ch",
            "Syria" to "sy",
            "Taiwan" to "tw",
            "Tajikistan" to "tj",
            "Thailand" to "th",
            "Timor-leste" to "tl",
            "Togo" to "tg",
            "Tonga" to "to",
            "Trinidad And Tobago" to "tt",
            "Turkey" to "tr",
            "Tuvalu" to "tv",
            "Ukraine" to "ua",
            "Uganda" to "ug",
            "United Arab Emirates" to "ae",
            "United Kingdom" to "gb",
            "United States Of America" to "us",
            "Uruguay" to "uy",
            "Uzbekistan" to "uz",
            "Vanuatu" to "vu",
            "Zambia" to "zm",
        )
    }

    companion object {
        private const val KEY_SELECTION_RESULT = "selected_result"
        private const val INTERFACE_LANGUAGE_SELECTION_RESULT =
            "interface_language_selection_result"
        private const val CONTENT_LANGUAGE_SELECTION_RESULT = "content_language_selection_result"
        private const val SOURCE_COUNTRY_LANGUAGE_SELECTION_RESULT =
            "source_country_language_selection_result"
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
        binding = FragmentSettingsBinding.inflate(layoutInflater)
        (activity as MainActivity).setSelectedMenuItem(R.id.settingsFragment)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory)[SettingsViewModel::class.java]
        observeViewModel()
        setupOnClickListener()
        binding.tvInterfaceLanguageValue.text =
            getLanguageName(viewModel.getInterfaceLanguage(), languageInterfaceMap)



        parentFragmentManager.setFragmentResultListener(
            INTERFACE_LANGUAGE_SELECTION_RESULT,
            this
        ) { _, bundle ->
            val selectedLanguage = bundle.getString(KEY_SELECTION_RESULT)
            selectedLanguage?.let {
                val languageCode = getLanguageCode(it, languageInterfaceMap)
                handleSelectedLanguage(languageCode)
                applyLocaleChanges()

            }
        }

        parentFragmentManager.setFragmentResultListener(
            CONTENT_LANGUAGE_SELECTION_RESULT,
            this
        ) { _, bundle ->
            val selectedLanguage = bundle.getString(KEY_SELECTION_RESULT)
            selectedLanguage?.let {
                val languageCode = getLanguageCode(it, languageContentLanguage)
                viewModel.saveContentLanguage(languageCode)
                applyLocaleChanges()

            }
        }

        parentFragmentManager.setFragmentResultListener(
            SOURCE_COUNTRY_LANGUAGE_SELECTION_RESULT,
            this
        ) { _, bundle ->
            val selectedSourceCountry = bundle.getString(KEY_SELECTION_RESULT)
            selectedSourceCountry?.let {
                val languageCode = getLanguageCode(it, languageSourceCountry)
                viewModel.saveSourceCountry(languageCode)
                applyLocaleChanges()

            }
        }
    }

    private fun getLanguageCode(
        selectedLanguage: String,
        mapLanguage: Map<String, String>
    ): String { // получение из полного названия языка в languagecode
        return mapLanguage[selectedLanguage] ?: LocaleHelper.SYSTEM_DEFAULT
    }

    private fun getLanguageName(
        languageCode: String,
        mapLanguage: Map<String, String>
    ): String { // получение из languagecode в полного названия языка
        return mapLanguage.entries.find { it.value == languageCode }?.key
            ?: LocaleHelper.SYSTEM_DEFAULT // Значение по умолчанию
    }

    private fun handleSelectedLanguage(language: String) {
        viewModel.saveInterfaceLanguage(language)
    }

    private fun setupOnClickListener() {
        binding.btSignIn.setOnClickListener {
            launchSignInFragment()
        }

        binding.tvLogout.setOnClickListener {
            viewModel.signOut()
            applyLocaleChanges()
        }


        binding.llContentLanguage.setOnClickListener {
            val languageName = binding.tvContentLanguageValue.text.toString()
            showLanguagePopup(LanguageOption.CONTENT_LANGUAGE, languageName)
        }
        binding.llInterfaceLanguage.setOnClickListener {
            val languageCode = viewModel.getInterfaceLanguage()
            val languageName = getLanguageName(languageCode, languageInterfaceMap)

            if (languageName == LocaleHelper.SYSTEM_DEFAULT)
                showLanguagePopup(
                    LanguageOption.INTERFACE_LANGUAGE,
                    requireContext().getString(R.string.system)
                )
            else
                showLanguagePopup(LanguageOption.INTERFACE_LANGUAGE, languageName)
        }
        binding.llNewsSourceCountry.setOnClickListener {
            val languageName = binding.tvNewsSourceCountryValue.text.toString()
            showLanguagePopup(LanguageOption.NEWS_SOURCE_COUNTRY, languageName)
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
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.userData.collect { user ->
                    if (user != null) {
                        binding.tvName.text = "${user.firstName} ${user.lastName}"
                        binding.tvEmailValue.text = user.email
                        binding.tvUsername.text = user.username
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED){
                viewModel.contentLanguage.collect{ languageCode ->
                    Log.d("CheckChangedUserData", "contentFromFragment$languageCode")

                    binding.tvContentLanguageValue.text =
                        getLanguageName(languageCode, languageContentLanguage)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED){
                viewModel.sourceCountry.collect{ countryCode ->
                    Log.d("CheckChangedUserData", "countryFromFragment$countryCode")

                    binding.tvNewsSourceCountryValue.text =
                        getLanguageName(countryCode, languageSourceCountry)
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
        parentFragmentManager.setFragmentResult("refresh_request", Bundle.EMPTY)
        requireActivity().recreate() // Перезапускает Activity, сохраняя состояние фрагментов.
    }

    private fun launchSignInFragment() {
        findNavController().navigate(
            SettingsFragmentDirections.actionSettingsFragmentToLoginFragment()
        )
    }

}

