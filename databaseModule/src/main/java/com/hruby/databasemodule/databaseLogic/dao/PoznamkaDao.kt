package com.hruby.databasemodule.databaseLogic.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hruby.databasemodule.data.Poznamka

@Dao
interface PoznamkaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoznamka(poznamka: Poznamka)

    @Update
    suspend fun updatePoznamka(poznamka: Poznamka)

    @Delete
    suspend fun deletePoznamka(poznamka: Poznamka)

    @Query("SELECT * FROM poznamka WHERE ulId = :ulId ORDER BY datum DESC")
    fun getPoznamkyByUlId(ulId: Int): LiveData<List<Poznamka>>
}