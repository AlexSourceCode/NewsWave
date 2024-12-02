package com.example.newswave.data.network.model

sealed class FilterCriteria {
    data class ByText(val text: String) : FilterCriteria()
    data class ByAuthor(val author: String) : FilterCriteria()
    data class ByDate(val date: String) : FilterCriteria()
}