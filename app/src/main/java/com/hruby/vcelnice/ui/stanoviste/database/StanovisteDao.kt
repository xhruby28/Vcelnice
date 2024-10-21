package com.hruby.vcelnice.ui.stanoviste.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.hruby.vcelnice.ui.stanoviste.Stanoviste

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
}