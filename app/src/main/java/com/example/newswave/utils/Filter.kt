package com.example.newswave.utils

import com.example.newswave.R

enum class Filter(val descriptionResId: Int, val parameterApi: String) {
    TEXT(R.string.FilterByText, "text"),
    AUTHOR(R.string.FilterByAuthor, "authors"),
    DATE(R.string.FilterByDate,"date")
}


