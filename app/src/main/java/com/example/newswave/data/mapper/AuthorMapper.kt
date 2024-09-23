package com.example.newswave.data.mapper

import com.example.newswave.data.database.dbAuthors.AuthorDbModel
import com.example.newswave.domain.entity.AuthorItemEntity
import javax.inject.Inject

class AuthorMapper @Inject constructor() {

    fun mapDbModelToAuthorEntity(dbModel: AuthorDbModel) = AuthorItemEntity(
        author = dbModel.author
    )
}