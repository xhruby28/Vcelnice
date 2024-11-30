package com.hruby.databasemodule.databaseLogic.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.hruby.databasemodule.data.Stanoviste
import com.hruby.databasemodule.databaseLogic.connections.StanovisteWithUly

@Dao
interface StanovisteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stanoviste: Stanoviste)

    @Update
    suspend fun update(stanoviste: Stanoviste)

    @Transaction
    suspend fun delete(stanoviste: Stanoviste) {
        // Smazání všech úlů přiřazených ke stanovišti
        deleteUlyByStanovisteId(stanoviste.id)
        // Poté smažte samotné stanoviště
        deleteStanovisteEntity(stanoviste)
    }

    @Query("DELETE FROM uly WHERE stanovisteId = :stanovisteId")
    suspend fun deleteUlyByStanovisteId(stanovisteId: Int) {
        // Tady budou volány privátní metody pro smazání závislých entit úlů
        deletePoznamkyByStanovisteId(stanovisteId)
        deleteMereneHodnotyByStanovisteId(stanovisteId)
        deleteProblemyByStanovisteId(stanovisteId)
    }

    @Query("DELETE FROM poznamka WHERE ulId IN (SELECT id FROM uly WHERE stanovisteId = :stanovisteId)")
    suspend fun deletePoznamkyByStanovisteId(stanovisteId: Int)

    @Query("DELETE FROM merene_hodnoty WHERE ulId IN (SELECT id FROM uly WHERE stanovisteId = :stanovisteId)")
    suspend fun deleteMereneHodnotyByStanovisteId(stanovisteId: Int)

    @Query("DELETE FROM problem WHERE ulId IN (SELECT id FROM uly WHERE stanovisteId = :stanovisteId)")
    suspend fun deleteProblemyByStanovisteId(stanovisteId: Int)

    @Delete
    suspend fun deleteStanovisteEntity(stanoviste: Stanoviste)

    @Query("SELECT * FROM stanoviste")
    fun getAllStanoviste(): LiveData<List<Stanoviste>>

    @Query("SELECT * FROM stanoviste WHERE id = :id")
    fun getStanovisteById(id: Int): LiveData<Stanoviste>

    @Transaction
    @Query("SELECT * FROM stanoviste WHERE id = :id")
    fun getStanovisteWithUly(id: Int): LiveData<StanovisteWithUly>

    @Query("SELECT * FROM stanoviste WHERE siteMAC = :macAddress")
    suspend fun getStanovisteByMAC(macAddress: String): Stanoviste?
}