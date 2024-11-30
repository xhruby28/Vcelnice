package com.hruby.databasemodule.databaseLogic.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hruby.databasemodule.data.MereneHodnoty
import com.hruby.databasemodule.data.Uly

@Dao
interface MereneHodnotyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMereneHodnoty(hodnoty: MereneHodnoty)

    @Update
    suspend fun updateMereneHodnoty(hodnoty: MereneHodnoty)

    @Delete
    suspend fun deleteMereneHodnoty(hodnoty: MereneHodnoty)

    @Query("""SELECT merene_hodnoty.* FROM merene_hodnoty
        INNER JOIN uly on merene_hodnoty.ulId = uly.id
        WHERE uly.stanovisteId = :stanovisteId AND uly.id = :ulId
        ORDER BY datum DESC""")
    fun getMereneHodnotyByUlIdAndStanovisteID(ulId: Int, stanovisteId: Int): LiveData<List<MereneHodnoty>>

    @Query("SELECT * FROM merene_hodnoty WHERE ulId = :ulId AND datum = :datum")
    suspend fun getMereneHodnotyByUlIdAndDate(ulId: Int, datum: Long): MereneHodnoty?

    @Query("SELECT * FROM uly WHERE id = :ulId AND macAddress = :mac LIMIT 1")
    suspend fun getUlByIdAndMAC(ulId: Int, mac: String): Uly?
}