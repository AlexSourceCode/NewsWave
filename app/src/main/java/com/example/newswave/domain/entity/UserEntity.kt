package com.example.newswave.domain.entity

data class UserEntity(
    val id: String,
    val username: String,
    val email: String,
    val password: String? = null,
    val firstName: String,
    val lastName: String,
    val newsContent: String,
    val newsSourceCountry: String
){
    constructor() : this("","","","","","","","")
}
