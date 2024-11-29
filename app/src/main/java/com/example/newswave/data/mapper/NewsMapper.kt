package com.example.newswave.data.mapper

import com.example.newswave.data.database.dbNews.NewsDbModel
import com.example.newswave.data.network.model.NewsItemDto
import com.example.newswave.data.network.model.TopNewsResponseDto
import com.example.newswave.domain.entity.AuthorItemEntity
import com.example.newswave.domain.entity.NewsItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NewsMapper @Inject constructor() {

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
            sourceCountry = dto.sourceCountry
        )
    }

    fun mapDbModelToEntity(dbModel: NewsDbModel) = NewsItemEntity(
        id = dbModel.id,
        title = dbModel.title,
        text = dbModel.text,
        url = dbModel.url,
        image = dbModel.image,
        video = dbModel.video,
        publishDate = dbModel.publishDate,
        author = dbModel.author,
        language = dbModel.language,
        sourceCountry = dbModel.sourceCountry
    )


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
            sourceCountry = dto.sourceCountry
        )
    }

    companion object {
        private const val EMPTY_CATEGORY = "unknownAuthor"
    }

}