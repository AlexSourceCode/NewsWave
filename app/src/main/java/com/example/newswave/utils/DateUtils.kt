package com.example.newswave.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.newswave.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal
import java.util.Locale

object DateUtils {

    // Получение даты публикации в нужном формате
    @SuppressLint("NewApi")
    fun dateFormat(context: Context, date: String): String {
        Log.d("CheckDateCur", date)
        val inputFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd' 'HH:mm:ss") // шаблон отображения времени
        val currentTimeNew = LocalDateTime.now() //текущее время
        val dateTime = LocalDateTime.parse(
            date,
            inputFormatter
        ) // преобразует строку в LocalDateTime в формате inputFormatter


        val minutesDiff =
            ChronoUnit.MINUTES.between(dateTime as Temporal, currentTimeNew as Temporal)
        val hoursDiff = ChronoUnit.HOURS.between(dateTime, currentTimeNew)
        val daysDiff = ChronoUnit.DAYS.between(dateTime, currentTimeNew)

        return when {
            minutesDiff < 60 -> context.getString(R.string.minutes_ago, minutesDiff)
            hoursDiff < 24 -> context.getString(R.string.hours_ago, hoursDiff)
            daysDiff == 1L -> context.getString(
                R.string.yesterday_at,
                dateTime.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
            )

            else -> dateTime.format(DateTimeFormatter.ofPattern(context.getString(R.string.date_format), Locale.getDefault()))
        }
    }

    // Получение сегодняшней даты
    @SuppressLint("NewApi")
    fun formatCurrentDate(): String {
        val currentDateTime = LocalDateTime.now()//текущая дата время
        val dateFormatted = DateTimeFormatter.ofPattern("yyyy-MM-dd") // шаблон отображения времени
        return currentDateTime.format(dateFormatted) //преобразование LocalDateTime в String по шаблону отображения
    }

    // Получение предыдущей даты
    @SuppressLint("NewApi")
    fun formatDateToYesterday(): String{
        val yesterdayDateTime = LocalDate.now().minusDays(1)
        val dateFormatted = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return yesterdayDateTime.format(dateFormatted)
    }
}