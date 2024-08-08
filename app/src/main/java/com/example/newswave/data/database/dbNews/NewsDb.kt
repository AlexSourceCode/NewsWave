package com.example.newswave.data.database.dbNews

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [NewsDbModel :: class], version = 2, exportSchema = false)
abstract class NewsDb : RoomDatabase() {

    abstract fun newsDao(): NewsDao

    companion object {

        private val LOCK = Any()
        private var db: NewsDb? = null
        private const val DB_NAME = "news.db"

        fun getInstance(context: Context): NewsDb {
            synchronized(LOCK) {
                db?.let { return it }
                val instance = Room.databaseBuilder(
                    context,
                    NewsDb :: class.java,
                    DB_NAME
                ).build()

                db = instance
                return instance
            }

        }
    }
}
