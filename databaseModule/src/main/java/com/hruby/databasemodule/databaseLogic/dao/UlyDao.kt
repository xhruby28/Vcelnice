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

    @Transaction
    suspend fun deleteUl(ul: Uly) {
        // Nejdříve smažte všechny závislé záznamy (měřené hodnoty, poznámky, problémy)
        deleteMereneHodnotyByUlId(ul.id)
        deletePoznamkyByUlId(ul.id)
        deleteProblemyByUlId(ul.id)
        deleteZaznamKontrolyByUlId(ul.id)
        // Poté smažte samotný úl
        deleteUlEntity(ul)
    }

    @Query("DELETE FROM merene_hodnoty WHERE ulId = :ulId")
    suspend fun deleteMereneHodnotyByUlId(ulId: Int)

    @Query("DELETE FROM poznamka WHERE ulId = :ulId")
    suspend fun deletePoznamkyByUlId(ulId: Int)

    @Query("DELETE FROM problem WHERE ulId = :ulId")
    suspend fun deleteProblemyByUlId(ulId: Int)

    @Query("DELETE FROM zaznam_kontroly WHERE ulId = :ulId")
    suspend fun deleteZaznamKontrolyByUlId(ulId: Int)

    @Delete
    suspend fun deleteUlEntity(ul: Uly)


    @Transaction
    @Query("SELECT * FROM uly WHERE stanovisteId = :stanovisteId ORDER BY uly.cisloUlu ASC")
    fun getUlyByStanovisteId(stanovisteId: Int): LiveData<List<Uly>>

    @Transaction
    @Query("SELECT * FROM uly WHERE id = :ulId AND stanovisteId = :stanovisteId")
    fun getUlWithOthersByStanovisteId(ulId: Int, stanovisteId: Int): LiveData<UlWithOther>

    @Transaction
    @Query("""
        SELECT uly.* FROM uly
        INNER JOIN stanoviste ON uly.stanovisteId = stanoviste.id
        WHERE uly.macAddress = :ulMacAddress
        AND stanoviste.siteMAC = :stanovisteMacAddress
        LIMIT 1
    """)
    suspend fun getUlWithOthersByMACAndStanovisteMAC(ulMacAddress: String,stanovisteMacAddress: String): Uly?

    @Query("SELECT COUNT(*) FROM uly WHERE stanovisteId = :stanovisteId")
    fun countUlyByStanovisteId(stanovisteId: Int): LiveData<Int>

}