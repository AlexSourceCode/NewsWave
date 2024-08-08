package com.example.newswave.data.database.dbNews

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {

    @Query("SELECT * FROM news")
    fun getNewsList(): Flow<List<NewsDbModel>>

    @Query("SELECT * FROM news  WHERE id = :id")
    fun getNewsDetailsById(id: Int): Flow<NewsDbModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(listNews: List<NewsDbModel>)//: Flow<NewsDbModel>


}

