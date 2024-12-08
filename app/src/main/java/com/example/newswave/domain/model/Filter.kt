package com.example.newswave.domain.model

import com.example.newswave.R

/**
 * Перечисление фильтров, применяемых для выборки данных.
 */
enum class Filter(val descriptionResId: Int, val parameterApi: String) {
    TEXT(R.string.FilterByText, "text"),
    AUTHOR(R.string.FilterByAuthor, "authors"),
    DATE(R.string.FilterByDate, "date")
}


