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
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Vytvoření tabulky 'uly'
                database.execSQL("""
            CREATE TABLE IF NOT EXISTS `uly` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `stanovisteId` INTEGER NOT NULL,
                `nazev` TEXT NOT NULL,
                `popis` TEXT NOT NULL,
                `problemovyUl` INTEGER NOT NULL,
                `posledniProblem` TEXT,
                `hodnoceni` INTEGER NOT NULL,
                `agresivita` INTEGER NOT NULL,
                `stavZasob` TEXT NOT NULL,
                `mezolitostPlodu` INTEGER NOT NULL,
                `silaVcelstva` INTEGER NOT NULL,
                `stavebniPud` INTEGER NOT NULL,
                FOREIGN KEY(`stanovisteId`) REFERENCES `stanoviste`(`id`) ON DELETE CASCADE
            )
        """)

                // Vytvoření tabulky 'problem'
                database.execSQL("""
            CREATE TABLE IF NOT EXISTS `problem` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `ulId` INTEGER NOT NULL,
                `datum` INTEGER NOT NULL,
                `text` TEXT NOT NULL,
                FOREIGN KEY(`ulId`) REFERENCES `uly`(`id`) ON DELETE CASCADE
            )
        """)

                // Vytvoření tabulky 'poznamka'
                database.execSQL("""
            CREATE TABLE IF NOT EXISTS `poznamka` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `ulId` INTEGER NOT NULL,
                `datum` INTEGER NOT NULL,
                `text` TEXT NOT NULL,
                FOREIGN KEY(`ulId`) REFERENCES `uly`(`id`) ON DELETE CASCADE
            )
        """)

                // Vytvoření tabulky 'merene_hodnoty'
                database.execSQL("""
            CREATE TABLE IF NOT EXISTS `merene_hodnoty` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `ulId` INTEGER NOT NULL,
                `datum` INTEGER NOT NULL,
                `hmotnost` REAL NOT NULL,
                `teplotaUl` REAL NOT NULL,
                `vlhkostModul` REAL NOT NULL,
                `teplotaModul` REAL NOT NULL,
                `frekvence` REAL NOT NULL,
                `gyroX` REAL NOT NULL,
                `gyroY` REAL NOT NULL,
                `gyroZ` REAL NOT NULL,
                `accelX` REAL NOT NULL,
                `accelY` REAL NOT NULL,
                `accelZ` REAL NOT NULL,
                FOREIGN KEY(`ulId`) REFERENCES `uly`(`id`) ON DELETE CASCADE
            )
        """)
            }
        }
    }
}