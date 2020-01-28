package com.omalik.android.hockeydj

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Track::class), version = 2, exportSchema = false)
abstract class TracksDatabase: RoomDatabase() {

    abstract fun trackDao(): TrackDao

    companion object {
        @Volatile
        private var INSTANCE: TracksDatabase? = null

        fun getDatabase(context: Context): TracksDatabase {

            return INSTANCE
                ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TracksDatabase::class.java,
                    "tracks_database"
                ).fallbackToDestructiveMigration().build()

                INSTANCE = instance
                instance
            }
        }
    }

}