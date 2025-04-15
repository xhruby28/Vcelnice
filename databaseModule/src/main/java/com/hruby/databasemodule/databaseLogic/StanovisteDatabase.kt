package com.hruby.databasemodule.databaseLogic

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hruby.databasemodule.data.MereneHodnoty
import com.hruby.databasemodule.data.Poznamka
import com.hruby.databasemodule.data.Problem
import com.hruby.databasemodule.data.Stanoviste
import com.hruby.databasemodule.data.Uly
import com.hruby.databasemodule.databaseLogic.dao.MereneHodnotyDao
import com.hruby.databasemodule.databaseLogic.dao.PoznamkaDao
import com.hruby.databasemodule.databaseLogic.dao.ProblemDao
import com.hruby.databasemodule.databaseLogic.dao.StanovisteDao
import com.hruby.databasemodule.databaseLogic.dao.UlyDao

@androidx.room.Database(entities = [Stanoviste::class, Uly::class, Poznamka::class, MereneHodnoty::class, Problem::class], version = 1, exportSchema = false)
abstract class StanovisteDatabase : RoomDatabase() {

    abstract fun stanovisteDao(): StanovisteDao
    abstract fun ulyDao(): UlyDao
    abstract fun poznamkaDao(): PoznamkaDao
    abstract fun mereneHodnotyDao(): MereneHodnotyDao
    abstract fun problemDao(): ProblemDao

    companion object {
        @Volatile
        private var INSTANCE: StanovisteDatabase? = null

        fun getDatabase(context: Context): StanovisteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StanovisteDatabase::class.java,
                    "stanoviste_database"
                )
                    //.addMigrations(MIGRATION_1_2)  // Přidání migrace pro verzi 1 → 2
                    //TODO("nezapomenout změnit v oficiálním release .fallbackToDestructiveMigration() na addMigrations()")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}