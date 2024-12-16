package com.example.newswave.data.repositories

import com.example.newswave.data.source.local.NewsDbModel
import com.example.newswave.data.source.local.UserPreferences
import com.example.newswave.data.mapper.NewsMapper
import com.example.newswave.data.network.api.ApiService
import com.example.newswave.data.network.models.NewsItemDto
import com.example.newswave.domain.entities.NewsItemEntity
import com.example.newswave.domain.repositories.RemoteDataSource
import com.example.newswave.domain.model.Filter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retry
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Реализация интерфейса RemoteDataSource, обеспечивающая работу с удаленным API для получения новостей
 * Использует ApiService для выполнения запросов и NewsMapper для преобразования данных
 */
class RemoteDataSourceImpl @Inject constructor(
    private val apiService: ApiService,
    private val mapper: NewsMapper,
    private val userPreferences: UserPreferences
) : RemoteDataSource {

    // Получает список топ-новостей с удаленного API
    override suspend fun fetchTopNews(
        date: String,
    ): List<NewsDbModel> {
        val sourceCountry = userPreferences.getSourceCountry()
        val language = userPreferences.getContentLanguage()
        return apiService.getListTopNews(
            sourceCountry = sourceCountry,
            language = language,
            date = date,
        ).map { mapper.mapJsonContainerTopNewsToListNews(flow { emit(it) }) }
            .flatMapConcat { newsList -> flow { emit(newsList.map { mapper.mapDtoToDbModel(it) }) } }
            .map { it.distinctBy { it.title } }
            .firstOrNull() ?: emptyList()
    }

    // Получает новости по текстовому запросу
    override suspend fun fetchNewsByText(
        text: String,
    ): Flow<List<NewsItemDto>> {
        val sourceCountry = userPreferences.getSourceCountry()
        val language = userPreferences.getContentLanguage()
        return apiService.getNewsByText(
            language = language,
            sourceCountry = sourceCountry,
            text = text,
        ).map { it.news }
    }

    // Получает новости по автору
    override suspend fun fetchNewsByAuthor(
        author: String,
    ): Flow<List<NewsItemDto>> {
        return apiService.getNewsByAuthor(
            author = author,
        ).map { it.news }
    }

    // Получает новости по дате публикации
    override suspend fun fetchNewsByDate(
        date: String,
    ): Flow<List<NewsItemDto>> {
        val sourceCountry = userPreferences.getSourceCountry()
        val language = userPreferences.getContentLanguage()
        return apiService.getNewsByDate(
            language = language,
            sourceCountry = sourceCountry,
            date = date
        ).map { mapper.mapJsonContainerTopNewsToListNews(flow { emit(it) }) }
    }

    // Получает новости с учетом фильтрации
    override suspend fun fetchFilteredNews(
        filterType: Filter,
        filterValue: String
    ): Flow<List<NewsItemEntity>> = flow {
        val response = when (filterType) {
            Filter.TEXT -> fetchNewsByText(filterValue)
            Filter.AUTHOR -> fetchNewsByAuthor(filterValue)
            Filter.DATE -> fetchNewsByDate(filterValue)
        }
        response
            .map { newsList ->
                newsList.map { newsItemDto -> mapper.mapDtoToEntity(newsItemDto) }
            }
            .map { newsEntities ->
                newsEntities.distinctBy { it.title }
            }
            .collect { distinctNews ->
                emit(distinctNews)
            }
    }
        .retry { cause ->
            if (cause is HttpException && cause.code() == 429) {
                delay(2000)
                true
            } else false
        }
        .map { newsList: List<NewsItemEntity> ->
            if (filterType == Filter.DATE) {
                newsList.sortedBy { it.publishDate }
            } else newsList
        }

    // Получает новости по автору и преобразует их в формат NewsItemEntity
    override suspend fun fetchNewsByAuthorFlow(author: String): Flow<List<NewsItemEntity>> =
        fetchNewsByAuthor(author).map { newsList ->
            newsList.map { mapper.mapDtoToEntity(it) }
        }
            .map { newsEntities ->
                newsEntities.distinctBy { it.title }
            }
            .catch { throw Exception("Error fetching news by author: $author. Cause: ${it.message}") }
}
