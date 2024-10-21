package com.hruby.vcelnice.ui.stanoviste.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hruby.vcelnice.ui.stanoviste.Stanoviste

@Database(entities = [Stanoviste::class], version = 1, exportSchema = false)
abstract class StanovisteDatabase : RoomDatabase() {

    abstract fun stanovisteDao(): StanovisteDao

    companion object {
        @Volatile
        private var INSTANCE: StanovisteDatabase? = null

        fun getDatabase(context: Context): StanovisteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StanovisteDatabase::class.java,
                    "stanoviste_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}