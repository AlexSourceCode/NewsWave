package com.example.newswave.utils

object TextUtils {
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