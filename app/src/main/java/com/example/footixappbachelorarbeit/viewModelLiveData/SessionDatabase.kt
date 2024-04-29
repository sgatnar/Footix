package com.example.footixappbachelorarbeit.viewModelLiveData

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Session::class], version = 1)//, exportSchema = false)
abstract class SessionDatabase : RoomDatabase() {

    abstract fun sessionDao(): SessionDao

    companion object {

        @Volatile
        private var INSTANCE: SessionDatabase? = null
        /*var MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Session ADD COLUMN id TEXT NOT NULL DEFAULT ''")
            }
        }*/

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
                ).build()//.addMigrations(MIGRATION_1_2)
                INSTANCE = instance
                return instance
            }
        }
    }
}
