package com.example.newswave.utils

object StringUtils {
    fun truncateText(text: String, maxLength: Int): String {
        return if (text.length > maxLength) {
            val lastSpaceIndex = text.lastIndexOf(' ', maxLength)
            if (lastSpaceIndex > 0) {
                text.substring(0, lastSpaceIndex) + "..."
            } else {
                text.take(maxLength) + "..."
            }
        } else {
            text
        }
    }
}