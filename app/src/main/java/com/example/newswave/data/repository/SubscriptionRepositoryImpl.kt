package com.example.newswave.data.repository

import android.app.Application
import com.example.newswave.R
import com.example.newswave.data.source.remote.FirebaseDataSource
import com.example.newswave.domain.entity.AuthorItemEntity
import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.domain.repository.RemoteDataSource
import com.example.newswave.domain.repository.SubscriptionRepository
import com.example.newswave.utils.NetworkUtils.isNetworkAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Реализация репозитория для управления подписками на авторов.
 */
class SubscriptionRepositoryImpl @Inject constructor(
    private val application: Application,
    private val remoteDataSource: RemoteDataSource,
    private val firebaseDataSource: FirebaseDataSource
) : SubscriptionRepository {

    private val ioScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO) // Определяет контекст для корутин на IO-потоке

    // Поток, содержащий текущего автора
    private val _currentAuthor = MutableSharedFlow<String?>()


    // Поток состояния новостей автора
    private val _authorNews = MutableSharedFlow<List<NewsItemEntity>>()
    private val authorNews: SharedFlow<List<NewsItemEntity>> get() = _authorNews.asSharedFlow()

    // Поток, указывающий, является ли автор избранным
    private var _isFavoriteAuthorFlow = MutableStateFlow<Boolean?>(null)
    private val isFavoriteAuthorFlow: StateFlow<Boolean?> get() = _isFavoriteAuthorFlow.asStateFlow()

    init {
        observeAuthorNews()
    }

    // Возвращает поток списка авторов
    override suspend fun getAuthorList(): SharedFlow<List<AuthorItemEntity>?> {
        if (!isNetworkAvailable(application)) {
            throw Exception(application.getString(R.string.no_internet_connection))
        }
        return firebaseDataSource.getAuthorListFlow()
    }

    // Загружает новости автора и возвращает поток новостей
    override suspend fun loadAuthorNews(author: String): SharedFlow<List<NewsItemEntity>> {
        if (!isNetworkAvailable(application)) {
            throw Exception(application.getString(R.string.no_internet_connection))
        }
        _currentAuthor.emit(author)
        return authorNews
    }


    // Подписывается на автора
    override suspend fun subscribeToAuthor(author: String) {
        val currentUser = firebaseDataSource.authStateFlow.value
        currentUser?.uid?.let { userId ->
            val authors = firebaseDataSource.fetchAuthors(userId)
            if (authors.none() { it.author == author }) {
                firebaseDataSource.addAuthor(userId, AuthorItemEntity(author))
                favoriteAuthorCheck(author)
            }
        }
    }

    // Отписывается от автора.
    override suspend fun unsubscribeFromAuthor(author: String) {
        val currentUser = firebaseDataSource.authStateFlow.value
        currentUser?.uid?.let { userId ->
            firebaseDataSource.deleteAuthor(userId, author)
            favoriteAuthorCheck(author)
        }
    }

    // Проверяет, является ли автор избранным, и обновляет состояние _isFavoriteAuthorFlow
    override fun favoriteAuthorCheck(author: String) {
        val currentUser = firebaseDataSource.authStateFlow.value
        ioScope.launch {
            val userId = currentUser?.uid ?: return@launch
            _isFavoriteAuthorFlow.value = firebaseDataSource.isFavoriteAuthor(userId, author)
        }
    }


    // Очищает состояние избранного автора
    override fun clearState() {
        _isFavoriteAuthorFlow.value = null
    }

    // Возвращает поток, указывающий, является ли автор избранным
    override fun isFavoriteAuthor(): StateFlow<Boolean?> = isFavoriteAuthorFlow

    // Наблюдает за новостями автора и обновляет _authorNews при изменении данных
    private fun observeAuthorNews() {
        ioScope.launch {
            _currentAuthor
                .filter {
                    delay(10) // crutch
                    val isConnected = isNetworkAvailable(application)
                    if (!isConnected) {
                        throw Exception(application.getString(R.string.no_internet_connection))
                    }
                    isConnected
                }
                .filterNotNull()
                .flatMapConcat { author ->
                    try {
                        remoteDataSource.fetchNewsByAuthorFlow(author)
                    } catch (e: Exception){
                        _authorNews.emit(emptyList())
                        flow{(emit(emptyList()))}
                    }
                }
                .filter { it.isNotEmpty() }
                .collect { news ->
                    _authorNews.emit(news)
                }
        }
    }
}
