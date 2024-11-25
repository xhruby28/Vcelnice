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
    @Query("SELECT * FROM uly WHERE id = :ulId AND stanovisteId = :stanovisteId")
    fun getUlWithOthersByStanovisteId(ulId: Int, stanovisteId: Int): LiveData<UlWithOther>

    @Transaction
    @Query("""
        SELECT * 
        FROM uly 
        INNER JOIN stanoviste ON uly.stanovisteId = stanoviste.id 
        WHERE uly.macAddress = :ulMacAddress AND stanoviste.siteMAC = :stanovisteMacAddress
        """)
    fun getUlWithOthersByMACAndStanovisteMAC(ulMacAddress: String, stanovisteMacAddress: String): LiveData<UlWithOther?>

    @Query("SELECT COUNT(*) FROM uly WHERE stanovisteId = :stanovisteId")
    fun countUlyByStanovisteId(stanovisteId: Int): LiveData<Int>
}