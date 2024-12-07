package com.example.newswave.domain.entity

/**
 * Представляет автора новостных статей
 */
data class AuthorItemEntity(
    val author: String
) {
    constructor() : this("")
}