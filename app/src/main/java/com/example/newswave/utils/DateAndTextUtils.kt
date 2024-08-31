package com.example.newswave.utils

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.example.newswave.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal
import java.util.Locale

object DateAndTextUtils {

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

    fun sentenceDivision(text: String): String {
        val textTemp = StringBuilder()
        var nextSentence = text
        var resultSize = 0
        val sentenceEndRegex =
            Regex("""(?<!\b\w\.\w)(?<!\b\w\.\w\.\w)(?<!\b\w\.\w\.\w\.\w)\.(?!\w)""")
        while (resultSize < 150) {
            val matchResult = sentenceEndRegex.find(nextSentence)
            if (matchResult != null) {
                val indexFinishedSentence = matchResult.range.last
                textTemp.append(nextSentence.substring(0, indexFinishedSentence + 1))
                resultSize = textTemp.length
                nextSentence = nextSentence.substring(indexFinishedSentence + 1)
            } else {
                break
            }

        }
        return textTemp.toString()
    }
}