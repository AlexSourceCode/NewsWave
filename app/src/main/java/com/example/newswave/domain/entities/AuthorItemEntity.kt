package com.example.newswave.domain.entities

/**
 * Представляет автора новостных статей
 */
data class AuthorItemEntity(
    val author: String
) {
    constructor() : this("")
}