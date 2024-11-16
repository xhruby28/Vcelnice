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

    @Delete
    suspend fun delete(stanoviste: Stanoviste)

    @Query("SELECT * FROM stanoviste")
    fun getAllStanoviste(): LiveData<List<Stanoviste>>

    @Query("SELECT * FROM stanoviste WHERE id = :id")
    fun getStanovisteById(id: Int): LiveData<Stanoviste>

    @Transaction
    @Query("SELECT * FROM stanoviste WHERE id = :id")
    fun getStanovisteWithUly(id: Int): LiveData<StanovisteWithUly>

    @Query("SELECT * FROM stanoviste WHERE siteMAC = :macAddress")
    fun getStanovisteByMAC(macAddress: String): LiveData<Stanoviste?>
}