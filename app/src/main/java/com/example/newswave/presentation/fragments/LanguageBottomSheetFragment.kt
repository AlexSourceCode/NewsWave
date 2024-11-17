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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newswave.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class LanguageBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var rootView: View
    private lateinit var behavior: BottomSheetBehavior<FrameLayout>
    private lateinit var recyclerView: RecyclerView
    private var screenHeight: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.popup_languages_settings, container, false)

        // Инициализация BottomSheetBehavior
        initializeBottomSheetBehavior()

        // Настройка RecyclerView
        setupRecyclerView()

        return rootView
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setDimAmount(0.4f) // Установка степени затемнения
            setOnShowListener { // вызывается когда диалог будет показан на экране
                val bottomSheet = findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
                bottomSheet.setBackgroundResource(android.R.color.transparent)

                // Получение BottomSheetBehavior
                behavior = BottomSheetBehavior.from(bottomSheet) // используется для получения экземпляра BottomSheetBehavior, ассоциированного с указанным View
                behavior.peekHeight = (screenHeight * 0.67).toInt()
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    private fun initializeBottomSheetBehavior() {
        val bottomSheet = rootView.findViewById<FrameLayout>(R.id.bottomSheetContainer)
        behavior = BottomSheetBehavior.from(bottomSheet)

        // Используем ViewTreeObserver для установки максимальной высоты
        bottomSheet.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() { //вызван, когда макет bottomSheet будет загружен и размещен на экране.
                bottomSheet.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val displayMetrics = resources.displayMetrics
                screenHeight = displayMetrics.heightPixels

                // Установка максимальной высоты
                bottomSheet.layoutParams.height = screenHeight

            }
        })

    }

    private fun setupRecyclerView() {
        recyclerView = rootView.findViewById(R.id.rvLanguages)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Получаем массив строк из ресурсов
        val languageArray = resources.getStringArray(R.array.languages_content_array).toList()

        // Создаем адаптер для RecyclerView
        val adapter = LanguageAdapter(languageArray)

        // Устанавливаем адаптер для RecyclerView
        recyclerView.adapter = adapter
    }
}

