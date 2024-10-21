package com.example.newswave.data.repository

import android.app.Application
import android.util.Log
import com.example.newswave.data.database.dbAuthors.AuthorDao
import com.example.newswave.data.database.dbAuthors.AuthorDbModel
import com.example.newswave.data.mapper.AuthorMapper
import com.example.newswave.data.mapper.NewsMapper
import com.example.newswave.data.network.api.ApiService
import com.example.newswave.domain.entity.AuthorItemEntity
import com.example.newswave.domain.entity.NewsItemEntity
import com.example.newswave.domain.model.NewsState
import com.example.newswave.domain.repository.SubscriptionRepository
import com.example.newswave.utils.NetworkUtils.isNetworkAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

class SubscriptionRepositoryImpl @Inject constructor(
    private val application: Application,
    private val authorDao: AuthorDao,
    private val mapper: AuthorMapper,
    private val mapperNews: NewsMapper,
    private val apiService: ApiService
) : SubscriptionRepository {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _currentAuthor = MutableSharedFlow<String?>()

    private val _authorNews = MutableSharedFlow<NewsState>()//mb stateflow
    private val authorNews: SharedFlow<NewsState> get() = _authorNews.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val getAuthorList =
        authorDao.getAuthorsList()
            .flatMapConcat { authorsList ->
                flow {
                    emit(authorsList.map { mapper.mapDbModelToAuthorEntity(it) })
                }
            }.stateIn(
                scope = coroutineScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = emptyList()
            )

    override suspend fun getAuthorList(): StateFlow<List<AuthorItemEntity>> = getAuthorList


    override suspend fun loadAuthorNews(author: String): SharedFlow<NewsState> {
        _currentAuthor.emit(author)
        return authorNews
    }


    override suspend fun subscribeToAuthor(author: String) {
        val author = AuthorDbModel(
            author = author
        )
        authorDao.insertAuthor(author)
    }

    override suspend fun unsubscribeFromAuthor(author: String) {
        authorDao.deleteAuthor(author)
    }

    override suspend fun favoriteAuthorCheck(author: String): Boolean =
        authorDao.isAuthorExists(author)

    init {
        coroutineScope.launch {
            _currentAuthor
                .filter {
                    delay(10)
                    val isConnected = isNetworkAvailable(application)
                    if (!isConnected) {
                        _authorNews.emit(NewsState.Error("No Internet connection"))
                    }
                    isConnected
                }
                .filterNotNull()
                .flatMapLatest { author ->
                    apiService.getNewsByAuthor(author = author)
                        .map { newsResponse ->
                            newsResponse.news.map { mapperNews.mapDtoToEntity(it) }
                                .distinctBy { it.title }
                        }
                }
                .catch {
                    _authorNews.emit(NewsState.Error(it.toString()))
                }
                .collect { news ->
                    _authorNews.emit(NewsState.Success(news))
                }
        }
    }
}