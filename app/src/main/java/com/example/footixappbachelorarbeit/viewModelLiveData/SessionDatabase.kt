package com.example.footixappbachelorarbeit.viewModelLiveData

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Session::class], version = 1)
abstract class SessionDatabase : RoomDatabase() {

    abstract fun sessionDao(): SessionDao

    companion object {

        @Volatile
        private var INSTANCE: SessionDatabase? = null

        fun getDatabase(context: Context): SessionDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SessionDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
