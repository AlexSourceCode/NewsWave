package com.example.newswave.presentation.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newswave.R
import com.example.newswave.databinding.PopupLanguagesSettingsBinding
import com.example.newswave.presentation.model.LanguageOption
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * BottomSheetDialogFragment для выбора языковых настроек
 */
class LanguageBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: PopupLanguagesSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var behavior: BottomSheetBehavior<FrameLayout>
    private var screenHeight: Int = 0
    private val args by navArgs<LanguageBottomSheetFragmentArgs>()

    companion object {
        private const val KEY_SELECTION_RESULT = "selected_result"
        private const val INTERFACE_LANGUAGE_SELECTION_RESULT =
            "interface_language_selection_result"
        private const val CONTENT_LANGUAGE_SELECTION_RESULT = "content_language_selection_result"
        private const val SOURCE_COUNTRY_LANGUAGE_SELECTION_RESULT =
            "source_country_language_selection_result"


        private const val BOTTOM_SHEET_DIM_AMOUNT = 0.4f
        private const val BOTTOM_SHEET_HEIGHT_RATIO = 0.67
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true // Разрешает закрытие по клику на фон
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PopupLanguagesSettingsBinding.inflate(inflater, container, false)

        // Инициализация BottomSheetBehavior
        initializeBottomSheetBehavior()

        // Настройка RecyclerView
        setupRecyclerView()

        return binding.root
    }

    // Конфигурирует настройки диалога, такие как затемнение и состояние развернутого BottomSheet
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setDimAmount(BOTTOM_SHEET_DIM_AMOUNT) // Установка степени затемнения
            setOnShowListener { // Вызывается, когда диалог будет показан на экране
                val bottomSheet =
                    findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
                bottomSheet.setBackgroundResource(android.R.color.transparent)

                // Получение BottomSheetBehavior
                behavior = BottomSheetBehavior.from(bottomSheet).apply {
                    skipCollapsed = true
                    state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }
    }

    // Инициализирует поведение BottomSheet, включая высоту и peekHeight
    private fun initializeBottomSheetBehavior() {
        val bottomSheet = binding.bottomSheetContainer
        behavior = BottomSheetBehavior.from(bottomSheet)

        bottomSheet.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                bottomSheet.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val displayMetrics = resources.displayMetrics
                screenHeight = displayMetrics.heightPixels

                // Вычисляем высоту BottomSheet как процент от высоты экрана
                val calculatedHeight = (screenHeight * BOTTOM_SHEET_HEIGHT_RATIO).toInt()
                bottomSheet.layoutParams = bottomSheet.layoutParams.apply {
                    height = calculatedHeight
                }
                bottomSheet.requestLayout()

                behavior.peekHeight = calculatedHeight
            }
        })
    }

    // Настраивает RecyclerView с адаптером и данными в зависимости от аргументов
    private fun setupRecyclerView() {
        val recyclerView = binding.rvLanguages
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Получаем массив языков и устанавливаем заголовок
        val languageArray = getLanguageArrayAndSetTitle()

        val adapter = LanguageListAdapter(languageArray)
        recyclerView.adapter = adapter

        // Выделяет текущий язык
        adapter.currentLanguageChecked = { listItem ->
            args.languageValue == listItem
        }
        // Обрабатывает выбор языка
        adapter.onLanguageClick = { language ->
            handleLanguageSelection(language)
        }
    }

    // Получает соответствующий массив языков и устанавливает заголовок в зависимости от аргументов
    private fun getLanguageArrayAndSetTitle(): List<String> {
        return when (args.languageOption) {
            LanguageOption.CONTENT_LANGUAGE -> {
                binding.tvTitle.text = getString(R.string.content_language)
                resources.getStringArray(R.array.languages_content_array).toList()
            }

            LanguageOption.INTERFACE_LANGUAGE -> {
                binding.tvTitle.text = getString(R.string.interface_language)
                resources.getStringArray(R.array.languages_interface_array).toList()
            }

            LanguageOption.NEWS_SOURCE_COUNTRY -> {
                binding.tvTitle.text = getString(R.string.country_of_news_source)
                resources.getStringArray(R.array.languages_source_country_array).toList()
            }
        }
    }

    // Обрабатывает логику выбора языка и возвращает результат родительскому фрагменту
    private fun handleLanguageSelection(language: String) {
        val resultKey = when (args.languageOption) {
            LanguageOption.INTERFACE_LANGUAGE -> INTERFACE_LANGUAGE_SELECTION_RESULT
            LanguageOption.CONTENT_LANGUAGE -> CONTENT_LANGUAGE_SELECTION_RESULT
            LanguageOption.NEWS_SOURCE_COUNTRY -> SOURCE_COUNTRY_LANGUAGE_SELECTION_RESULT
        }
        launchSettingsFragment(language, resultKey)
    }

    // Возвращает выбранный язык родительскому фрагменту и закрывает диалог
    private fun launchSettingsFragment(language: String, resultKey: String) {
        val resultBundle = Bundle().apply {
            putString(KEY_SELECTION_RESULT, language)
        }
        parentFragmentManager.setFragmentResult(resultKey, resultBundle)
        dismiss() // Закрыть BottomSheet
    }

    // Очищает привязку к представлениям, чтобы избежать утечек памяти
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
