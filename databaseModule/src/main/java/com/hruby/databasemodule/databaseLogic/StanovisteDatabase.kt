package com.hruby.databasemodule.databaseLogic

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hruby.databasemodule.data.Stanoviste

@androidx.room.Database(entities = [Stanoviste::class], version = 1, exportSchema = false)
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