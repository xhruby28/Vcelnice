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
import com.hruby.databasemodule.data.ZaznamKontroly
import com.hruby.databasemodule.databaseLogic.dao.MereneHodnotyDao
import com.hruby.databasemodule.databaseLogic.dao.PoznamkaDao
import com.hruby.databasemodule.databaseLogic.dao.ProblemDao
import com.hruby.databasemodule.databaseLogic.dao.StanovisteDao
import com.hruby.databasemodule.databaseLogic.dao.UlyDao
import com.hruby.databasemodule.databaseLogic.dao.ZaznamKontrolyDao

@androidx.room.Database(
    entities = [Stanoviste::class, Uly::class, Poznamka::class, MereneHodnoty::class, Problem::class, ZaznamKontroly::class],
    version = 8,
    exportSchema = false)
abstract class StanovisteDatabase : RoomDatabase() {

    abstract fun stanovisteDao(): StanovisteDao
    abstract fun ulyDao(): UlyDao
    abstract fun poznamkaDao(): PoznamkaDao
    abstract fun mereneHodnotyDao(): MereneHodnotyDao
    abstract fun problemDao(): ProblemDao
    abstract fun zaznamKontrolyDao(): ZaznamKontrolyDao

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
                    //TODO("nezapomenout změnit v oficiálním release .fallbackToDestructiveMigration() na addMigrations()")
                    //.fallbackToDestructiveMigration()
                    .addMigrations(MIGRATION_2_3,MIGRATION_3_4,MIGRATION_4_5,MIGRATION_5_6,MIGRATION_6_7,MIGRATION_7_8)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE zaznam_kontroly ADD COLUMN problemMatkaNeni INTEGER DEFAULT 0")
                database.execSQL("ALTER TABLE zaznam_kontroly ADD COLUMN problemMatkaMednik INTEGER DEFAULT 0")
                database.execSQL("ALTER TABLE zaznam_kontroly ADD COLUMN problemMatkaNeklade INTEGER DEFAULT 0")
                database.execSQL("ALTER TABLE zaznam_kontroly ADD COLUMN problemMatkaVybehliMatecnik INTEGER DEFAULT 0")
                database.execSQL("ALTER TABLE zaznam_kontroly ADD COLUMN problemMatkaZvapenatelyPlod INTEGER DEFAULT 0")
                database.execSQL("ALTER TABLE zaznam_kontroly ADD COLUMN problemMatkaNosema INTEGER DEFAULT 0")
                database.execSQL("ALTER TABLE zaznam_kontroly ADD COLUMN problemMatkaLoupezOtevrena INTEGER DEFAULT 0")
                database.execSQL("ALTER TABLE zaznam_kontroly ADD COLUMN problemMatkaLoupezSkryta INTEGER DEFAULT 0")
                database.execSQL("ALTER TABLE zaznam_kontroly ADD COLUMN problemMatkaTrubcice INTEGER DEFAULT 0")
                database.execSQL("ALTER TABLE zaznam_kontroly ADD COLUMN problemMatkaJine INTEGER DEFAULT 0")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE uly ADD COLUMN matkaVidena INTEGER DEFAULT 0")
                database.execSQL("ALTER TABLE uly ADD COLUMN matkaVidenaDatum INTEGER")
                database.execSQL("ALTER TABLE uly ADD COLUMN matkaOznaceni TEXT")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE uly ADD COLUMN matkaBarva TEXT")
                database.execSQL("ALTER TABLE uly ADD COLUMN matkaRok TEXT")
                database.execSQL("ALTER TABLE uly ADD COLUMN matkaKridla INTEGER DEFAULT 0")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE uly ADD COLUMN lastKontrola TEXT")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE uly ADD COLUMN lastKontrolaDate INTEGER")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE uly ADD COLUMN createdTimestamp INTEGER")
            }
        }
    }
}