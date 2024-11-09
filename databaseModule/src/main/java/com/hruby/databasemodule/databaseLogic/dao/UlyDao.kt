package com.hruby.databasemodule.databaseLogic.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.hruby.databasemodule.data.Uly
import com.hruby.databasemodule.databaseLogic.connections.UlWithOther

@Dao
interface UlyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUl(ul: Uly)

    @Update
    suspend fun updateUl(ul: Uly)

    @Delete
    suspend fun deleteUl(ul: Uly)

    @Transaction
    @Query("SELECT * FROM uly WHERE stanovisteId = :stanovisteId")
    fun getUlyByStanovisteId(stanovisteId: Int): LiveData<List<Uly>>

    @Transaction
    @Query("SELECT * FROM uly WHERE stanovisteId = :stanovisteId")
    fun getUlWithOthersByStanovisteId(stanovisteId: Int): LiveData<List<UlWithOther>>
}