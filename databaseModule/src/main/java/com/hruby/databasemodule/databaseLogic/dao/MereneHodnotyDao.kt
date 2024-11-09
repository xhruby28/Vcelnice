package com.hruby.databasemodule.databaseLogic.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hruby.databasemodule.data.MereneHodnoty

@Dao
interface MereneHodnotyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMereneHodnoty(hodnoty: MereneHodnoty)

    @Update
    suspend fun updateMereneHodnoty(hodnoty: MereneHodnoty)

    @Delete
    suspend fun deleteMereneHodnoty(hodnoty: MereneHodnoty)

    @Query("SELECT * FROM merene_hodnoty WHERE ulId = :ulId ORDER BY datum DESC")
    fun getMereneHodnotyByUlId(ulId: Int): LiveData<List<MereneHodnoty>>
}