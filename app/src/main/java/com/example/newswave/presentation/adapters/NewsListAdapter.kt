package com.example.newswave.presentation.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.example.newswave.databinding.NewsItemBinding
import com.example.newswave.domain.NewsInfo
import com.squareup.picasso.Picasso
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal
import java.util.Locale

class NewsListAdapter(
    private val context: Context
) : ListAdapter<NewsInfo, NewsListViewHolder>(NewsListDiffCallback) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsListViewHolder {
        val binding = NewsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsListViewHolder, position: Int) {
        val news = getItem(position)
        with(holder.binding) {
            with(news) {
                tvText.text = sentenceDivision(news.text)
                tvTitle.text = news.title
                tvDate.text = dateFormat(news.publishDate)
                Picasso.get().load(image).into(ivImage)
            }
        }
    }


    @SuppressLint("NewApi")
    private fun dateFormat(date: String): String {
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
            minutesDiff < 60 -> "$minutesDiff минут назад"
            hoursDiff < 24 -> "$hoursDiff часов назад"
            daysDiff == 1L -> "Вчера в ${
                dateTime.toString().format(DateTimeFormatter.ofPattern("HH:mm"))
            }"

            else -> dateTime.format(DateTimeFormatter.ofPattern("d MMMM 'в' HH:mm", Locale("ru")))

        }

    }

    private fun sentenceDivision(text: String): String {
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