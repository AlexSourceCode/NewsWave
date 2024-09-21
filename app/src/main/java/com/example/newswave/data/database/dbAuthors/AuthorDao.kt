package com.example.newswave.data.database.dbAuthors

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthorDao {
    @Query("SELECT * FROM favorite")
    fun getAuthorsList(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAuthor(author: AuthorDbModel)


    @Query("DELETE FROM favorite WHERE author = :author")
    suspend fun deleteAuthor(author: String)
}

