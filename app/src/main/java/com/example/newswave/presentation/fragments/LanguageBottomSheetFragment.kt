package com.example.newswave.presentation.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newswave.R
import com.example.newswave.databinding.PopupLanguagesSettingsBinding
import com.example.newswave.utils.LanguageOption
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LanguageBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: PopupLanguagesSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var behavior: BottomSheetBehavior<FrameLayout>
    private var screenHeight: Int = 0
    private val args by navArgs<LanguageBottomSheetFragmentArgs>()

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
            window?.setDimAmount(0.4f) // Установка степени затемнения
            setOnShowListener { // вызывается когда диалог будет показан на экране
                val bottomSheet = findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
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

        bottomSheet.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                bottomSheet.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val displayMetrics = resources.displayMetrics
                screenHeight = displayMetrics.heightPixels

                val calculatedHeight = (screenHeight * 0.67).toInt()
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
            LanguageOption.CONTENT_LANGUAGE -> resources.getStringArray(R.array.languages_content_array).toList()
            LanguageOption.INTERFACE_LANGUAGE -> resources.getStringArray(R.array.languages_interface_array).toList()
            LanguageOption.NEWS_SOURCE_COUNTRY -> resources.getStringArray(R.array.languages_source_country_array).toList()
        }

        // Создаем адаптер для RecyclerView
        val adapter = LanguageAdapter(languageArray)

        // Устанавливаем адаптер для RecyclerView
        recyclerView.adapter = adapter

        recyclerView.setOnTouchListener { _, event -> // Перехват касаний
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    if (!recyclerView.canScrollVertically(-1)) {
                        // Если RecyclerView не может прокручиваться вверх (на первом элементе)
                        binding.root.parent?.requestDisallowInterceptTouchEvent(false)
                    }
                }
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP -> {
                    // Передаем управление жестами родителю
                    binding.root.parent?.requestDisallowInterceptTouchEvent(true)
                }
            }
            false // Возвращаем false, чтобы RecyclerView продолжал обрабатывать события прокрутки
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Очищаем binding, чтобы избежать утечек памяти
    }
}
