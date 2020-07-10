package com.example.movielab.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.movielab.data.db.dao.MovieDao
import com.example.movielab.data.db.entity.MovieEntity


@Database(
    entities = [MovieEntity::class],
    version = 1
)

//Database
abstract class MovieLabDatabase : RoomDatabase() {

    abstract fun movieDao(): MovieDao

    companion object {
        @Volatile
        private var instance: MovieLabDatabase? = null
        private val LOCK = Any()

        fun getAppDataBase(context: Context): MovieLabDatabase? {
            if (instance == null) {
                synchronized(MovieLabDatabase::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        MovieLabDatabase::class.java,
                        "movie.db"
                    ).build()
                }
            }
            return instance
        }

    }

}