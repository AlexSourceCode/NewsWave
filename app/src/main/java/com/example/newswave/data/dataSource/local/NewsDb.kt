package com.example.newswave.data.dataSource.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 *  NewsDb: Класс базы данных Room
 */
@Database(entities = [NewsDbModel::class], version = 3, exportSchema = false)
abstract class NewsDb : RoomDatabase() {

    // Возвращает DAO для взаимодействия с таблицей новостей
    abstract fun newsDao(): NewsDao

    companion object {

        private val LOCK = Any()
        private var db: NewsDb? = null
        private const val DB_NAME = "news.db"

        // Получение экземпляра базы данных.
        fun getInstance(context: Context): NewsDb {
            synchronized(LOCK) {
                db?.let { return it }
                val instance = Room.databaseBuilder(
                    context,
                    NewsDb::class.java,
                    DB_NAME
                ).fallbackToDestructiveMigration().build()

                db = instance
                return instance
            }

        }
    }
}
