package com.example.newswave.presentation.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.text.toUpperCase
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newswave.R
import com.example.newswave.databinding.PopupLanguagesSettingsBinding
import com.example.newswave.utils.LanguageOption
import com.example.newswave.utils.LocaleHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Locale

class LanguageBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: PopupLanguagesSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var behavior: BottomSheetBehavior<FrameLayout>
    private var screenHeight: Int = 0
    private val args by navArgs<LanguageBottomSheetFragmentArgs>()

    companion object{
        private const val KEY_SELECTED_LANGUAGE = "selected_language"
        private const val LANGUAGE_SELECTION_RESULT = "language_selection_result"

        private const val BOTTOM_SHEET_DIM_AMOUNT = 0.4f
        private const val BOTTOM_SHEET_HEIGHT_RATIO = 0.67
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true // Разрешаем закрытие по клику на фон
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setDimAmount(BOTTOM_SHEET_DIM_AMOUNT) // Установка степени затемнения
            setOnShowListener { // вызывается когда диалог будет показан на экране
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

    private fun initializeBottomSheetBehavior() {
        val bottomSheet = binding.bottomSheetContainer
        behavior = BottomSheetBehavior.from(bottomSheet)

        bottomSheet.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                bottomSheet.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val displayMetrics = resources.displayMetrics
                screenHeight = displayMetrics.heightPixels

                val calculatedHeight = (screenHeight * BOTTOM_SHEET_HEIGHT_RATIO).toInt()
                bottomSheet.layoutParams = bottomSheet.layoutParams.apply {
                    height = calculatedHeight
                }
                bottomSheet.requestLayout()

                behavior.peekHeight = calculatedHeight
            }
        })
    }

    private fun setupRecyclerView() {
        val recyclerView = binding.rvLanguages
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Получаем массив строк из ресурсов
        val languageArray = when (args.languageOption) {
            LanguageOption.CONTENT_LANGUAGE -> resources.getStringArray(R.array.languages_content_array)
                .toList()

            LanguageOption.INTERFACE_LANGUAGE -> resources.getStringArray(R.array.languages_interface_array)
                .toList()

            LanguageOption.NEWS_SOURCE_COUNTRY -> resources.getStringArray(R.array.languages_source_country_array)
                .toList()
        }

        // Создаем адаптер для RecyclerView
        val adapter = LanguageListAdapter(languageArray)

        // Устанавливаем адаптер для RecyclerView
        recyclerView.adapter = adapter

        adapter.currentLanguageChecked = { listItem ->
            if (args.languageValue == ""){

            }
            args.languageValue == listItem
        }

        adapter.onLanguageClick = { language ->
            launchSettingsFragment(language)
        }

    }

    private fun launchSettingsFragment(language: String){
        val resultBundle = Bundle().apply {
            putString(KEY_SELECTED_LANGUAGE, language)
        }
        parentFragmentManager.setFragmentResult(LANGUAGE_SELECTION_RESULT, resultBundle)
        dismiss() // Закрыть BottomSheet
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Очищаем binding, чтобы избежать утечек памяти
    }
}
