package com.example.newswave.data.dataSource.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * NewsDao: Интерфейс для взаимодействия с базой данных через Room
 */
@Dao
interface NewsDao {

    // Получение списка новостей из базы данных в виде потока Flow
    // Результат отсортирован по дате публикации по убыванию
    @Query("SELECT * FROM news ORDER BY publishDate DESC")
    fun getNewsList(): Flow<List<NewsDbModel>>

    // Вставка списка новостей в базу данных. При конфликте данные перезаписываются
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(listNews: List<NewsDbModel>)

    // Удаление всех записей из таблицы новостей.
    @Query("DELETE FROM news")
    fun deleteAllNews()

}

