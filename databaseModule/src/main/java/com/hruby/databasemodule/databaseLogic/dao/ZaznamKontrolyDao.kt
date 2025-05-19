package com.hruby.databasemodule.databaseLogic.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hruby.databasemodule.data.ZaznamKontroly

@Dao
interface ZaznamKontrolyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertZaznam(zaznam: ZaznamKontroly)

    @Update
    suspend fun updateZaznam(zaznam: ZaznamKontroly)

    @Delete
    suspend fun deleteZaznam(zaznam: ZaznamKontroly)

    @Query("SELECT * FROM zaznam_kontroly WHERE ulId = :ulId ORDER BY datum DESC")
    fun getZaznamyByUlId(ulId: Int): LiveData<List<ZaznamKontroly>>

    // Poslední záznam pro každý úl pod daným stanovistem
    @Query("""
        SELECT zk.*
        FROM zaznam_kontroly zk
        INNER JOIN uly u ON zk.ulId = u.id
        WHERE u.stanovisteId = :stanovisteId AND zk.datum = (
            SELECT MAX(datum) FROM zaznam_kontroly WHERE ulId = zk.ulId
        )
        ORDER BY zk.datum DESC
    """)
    fun getLastZaznamyByStanovisteId(stanovisteId: Int): LiveData<List<ZaznamKontroly>>
}