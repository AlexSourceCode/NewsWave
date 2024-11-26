package com.example.newswave.data.database.dbNews

import android.util.Log
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {

    @Query("SELECT * FROM news ORDER BY publishDate DESC")
    fun getNewsList(): Flow<List<NewsDbModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(listNews: List<NewsDbModel>)//: Flow<NewsDbModel>

    @Query("DELETE FROM news")
    fun deleteAllNews()


}

