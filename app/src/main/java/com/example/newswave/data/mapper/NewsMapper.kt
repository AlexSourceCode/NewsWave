package com.example.newswave.data.mapper

import android.util.Log
import com.example.newswave.data.database.dbNews.NewsDbModel
import com.example.newswave.data.network.model.NewsItemDto
import com.example.newswave.data.network.model.NewsResponseDto
import com.example.newswave.domain.NewsInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NewsMapper {

    fun mapDtoToDbModel(dto: NewsItemDto): NewsDbModel {
//        if (dto.category == null) {
//            Log.d("CheckJson","Category is null for DTO with id: ${dto.title}")
//        }
//        val category = dto.category ?: "Uncategorized"
        val author = dto.author ?: EMPTY_CATEGORY
//        Log.d("CheckCategory", dto.category.toString())

        return NewsDbModel(
            id = dto.id,
            title = dto.title,
            text = dto.text,
            url = dto.url,
            image = dto.image,
            video = dto.video,
            publishDate = dto.publishDate,
            author = author,
            language = dto.language,
//            category = category,
            sourceCountry = dto.sourceCountry
        )
    }

    suspend fun mapJsonContainerToListNews(newsResponseDto: Flow<NewsResponseDto>): List<NewsItemDto> {
        val result = mutableListOf<NewsItemDto>()
        val newsTopDto = newsResponseDto.map { it.news }.flattenToList()
        for (itemTop in newsTopDto) {
            for (item in itemTop.newsTop) {
                result.add(item)
            }
        }
        return result

    }

    fun dbModelToEntity(dbModel: NewsDbModel) = NewsInfo(
        id = dbModel.id,
        title = dbModel.title,
        text = dbModel.text,
        url = dbModel.url,
        image = dbModel.image,
        video = dbModel.video,
        publishDate = dbModel.publishDate,
        author = dbModel.author,
        language = dbModel.language,
//        category = dbModel.category,
        sourceCountry = dbModel.sourceCountry
    )

    companion object{
        private const val EMPTY_CATEGORY = "unknownAuthor"
    }

}