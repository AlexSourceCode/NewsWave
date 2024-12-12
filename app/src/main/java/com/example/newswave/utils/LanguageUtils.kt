package com.example.newswave.utils

import android.app.Application
import com.example.newswave.R

object LanguageUtils {

    private lateinit var application: Application

    val interfaceMap by lazy {
        mapOf(
            application.getString(R.string.system) to LocaleHelper.SYSTEM_DEFAULT, // Устройство по умолчанию
            "Русский" to "ru", // Русский
            "English" to "en"  // Английский
        )
    }
    val contentLanguage by lazy {
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
    val sourceCountry by lazy {
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

    fun initialize(app: Application) {
        application = app
    }

    fun getLanguageCode(selectedLanguage: String, mapLanguage: Map<String, String>): String =
        mapLanguage[selectedLanguage] ?: LocaleHelper.SYSTEM_DEFAULT


    fun getLanguageName(languageCode: String, mapLanguage: Map<String, String>): String =
        mapLanguage.entries.find { it.value == languageCode }?.key ?: LocaleHelper.SYSTEM_DEFAULT
}