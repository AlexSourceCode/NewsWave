package com.example.newswave.data.mapper

import com.example.newswave.data.database.dbNews.NewsDbModel
import com.example.newswave.data.network.model.NewsItemDto
import com.example.newswave.data.network.model.TopNewsResponseDto
import com.example.newswave.domain.NewsItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NewsMapper {



    suspend fun mapJsonContainerTopNewsToListNews(topNewsResponseDto: Flow<TopNewsResponseDto>): List<NewsItemDto> {
        val result = mutableListOf<NewsItemDto>()
        val newsTopDto = topNewsResponseDto.map { it.news }.flattenToList()// преобразование в List<NewsTopDto>
        for (itemTop in newsTopDto) {
            for (item in itemTop.newsTop) {
                result.add(item)
            }
        }
        return result

    }
    fun mapDtoToDbModel(dto: NewsItemDto): NewsDbModel {
        val author = dto.author ?: EMPTY_CATEGORY

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

    fun dbModelToEntity(dbModel: NewsDbModel) = NewsItemEntity(
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

//    suspend fun mapJsonContainerNewsToListNews(newsResponseDto: NewsResponseDto): List<NewsItemDto>{
//        val result = mutableListOf<NewsItemDto>()
//        val news = newsResponseDto.news
//    }

    fun mapDtoToEntity(dto: NewsItemDto): NewsItemEntity {
        val author = dto.author ?: EMPTY_CATEGORY

        return NewsItemEntity(
            id = dto.id,
            title = dto.title,
            text = dto.text,
            url = dto.url,
            image = dto.image,
            video = dto.video,
            publishDate = dto.publishDate,
            author = author,
            language = dto.language,
//        category = dto.category,
            sourceCountry = dto.sourceCountry
        )
    }

    companion object {
        private const val EMPTY_CATEGORY = "unknownAuthor"
    }

}