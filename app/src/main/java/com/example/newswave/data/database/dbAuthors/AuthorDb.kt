package com.example.newswave.data.database.dbAuthors

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//@Database(entities = [AuthorDbModel:: class], version = 2, exportSchema = false)
//abstract class AuthorDb : RoomDatabase() {
//
//    abstract fun authorDao(): AuthorDao
//
//
//
//    companion object {
//        private val LOCK = Any()
//        private var db: AuthorDb? = null
//        private const val DB_NAME = "favorite.db"
//
//        fun getInstance(context: Context): AuthorDb {
//
//            synchronized(LOCK) {
//                db?.let { return it }
//                val instance = Room.databaseBuilder(
//                    context, AuthorDb::class.java,
//                    DB_NAME
//                ).build()
//                db = instance
//                return instance
//            }
//        }
//    }
//
//}