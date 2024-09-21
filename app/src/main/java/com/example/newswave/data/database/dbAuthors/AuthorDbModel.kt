package com.example.newswave.data.database.dbAuthors

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite")
data class AuthorDbModel(
    @PrimaryKey
//    val id: Int,
//    val title: String,
//    val text: String,
//    val url: String,
//    val image: String?,
//    val video: String?,
//    val publishDate: String,
    val author: String,
//    val language: String,
//    val category: String,
//    val sourceCountry: String
)
