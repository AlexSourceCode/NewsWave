package com.example.newswave.data.database.dbAuthors

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

//@Dao
//interface AuthorDao {
//    @Query("SELECT * FROM favorite")
//    suspend fun getAuthorsList(): Flow<List<String>>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertAuthor(author: String)
//
//    @Query("DELETE FROM favorite WHERE author = :author")
//    suspend fun deleteAuthorByAuthor(author: String)
//}

