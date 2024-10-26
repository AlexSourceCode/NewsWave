package com.example.newswave.data.repository

import android.app.Application
import android.util.Log
import com.example.newswave.data.mapper.NewsMapper
import com.example.newswave.data.network.api.ApiService
import com.example.newswave.domain.entity.AuthorItemEntity
import com.example.newswave.domain.model.NewsState
import com.example.newswave.domain.repository.SubscriptionRepository
import com.example.newswave.utils.NetworkUtils.isNetworkAvailable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class SubscriptionRepositoryImpl @Inject constructor(
    private val application: Application,
    private val mapperNews: NewsMapper,
    private val apiService: ApiService,
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth
) : SubscriptionRepository {

    private val ioScope = CoroutineScope(Dispatchers.IO)

    private val _currentAuthor = MutableSharedFlow<String?>()

    private val _authorNews = MutableSharedFlow<NewsState>()//mb stateflow
    private val authorNews: SharedFlow<NewsState> get() = _authorNews.asSharedFlow()

    private val _authorList = MutableStateFlow<List<AuthorItemEntity>?>(null)
    private val authorList: StateFlow<List<AuthorItemEntity>?> get() = _authorList.asStateFlow()

    private val _isFavoriteAuthorFlow = MutableStateFlow<Boolean?>(null)
    private val isFavoriteAuthorFlow: StateFlow<Boolean?> get() = _isFavoriteAuthorFlow.asStateFlow()

    private val authorsReference = database.getReference("Authors")


    override suspend fun getAuthorList(): StateFlow<List<AuthorItemEntity>?> = authorList


    override suspend fun loadAuthorNews(author: String): SharedFlow<NewsState> {
        _currentAuthor.emit(author)
        return authorNews
    }


    override suspend fun subscribeToAuthor(author: String) {
        val authorEntity = AuthorItemEntity(author)
        val userId = auth.currentUser?.uid.toString()
        val authorQuery = authorsReference.child(userId)
            .orderByChild("author")
            .equalTo(author)

        authorQuery.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    return
                } else{
                    authorsReference.child(userId).push().setValue(authorEntity)
                    ioScope.launch {
                        favoriteAuthorCheck(author)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("UserRepositoryImpl", "Failed to read value.", error.toException())
            }

        })
    }

    override suspend fun unsubscribeFromAuthor(author: String) {
        val userId = auth.currentUser?.uid.toString()
        val authorQuery = authorsReference.child(userId).orderByChild("author")

        authorQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (authorSnapshot in snapshot.children) {
                    val authorEntity = authorSnapshot.getValue(AuthorItemEntity::class.java)
                    if (authorEntity?.author == author) {
                        authorSnapshot.ref.removeValue()
                        ioScope.launch {
                            favoriteAuthorCheck(author)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("UserRepositoryImpl", "Failed to remove value.", error.toException())
            }
        })
    }

    override suspend fun favoriteAuthorCheck(author: String) {
            authorsReference.child(auth.currentUser?.uid.toString())
                .orderByChild("author")
                .equalTo(author)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        _isFavoriteAuthorFlow.value = snapshot.exists()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        error.toException()
                    }
                })
        }

    override fun isFavoriteAuthor(): StateFlow<Boolean?> = isFavoriteAuthorFlow


    init {
        auth.addAuthStateListener {
            authorsReference.child(auth.currentUser?.uid.toString())
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.w("UserRepositoryImpl", "flagdblsitener")
                        val authors = mutableListOf<AuthorItemEntity>()
                        for (authorSnapshot in snapshot.children) {
                            val author = authorSnapshot.getValue(AuthorItemEntity::class.java)
                            author?.let { authors.add(author) }
                        }
                        _authorList.value = authors
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.w("UserRepositoryImpl", "Failed to read value.", error.toException())
                    }
                })
        }


        ioScope.launch {
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
