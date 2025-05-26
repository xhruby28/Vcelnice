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

    @Query("SELECT * FROM zaznam_kontroly WHERE ulId = :ulId ORDER BY datum DESC, createdTimestamp DESC")
    fun getZaznamyByUlId(ulId: Int): LiveData<List<ZaznamKontroly>>

    @Query("SELECT * FROM zaznam_kontroly WHERE ulId = :ulId ORDER BY datum DESC, createdTimestamp DESC LIMIT 1")
    fun getLastZaznamForUl(ulId: Int): LiveData<ZaznamKontroly?>

    @Query("SELECT * FROM zaznam_kontroly WHERE ulId = :ulId AND matkaVidena = 1 ORDER BY datum DESC, createdTimestamp DESC LIMIT 1")
    fun getLastMatkaVidenaByUlId(ulId: Int): LiveData<ZaznamKontroly?>

    @Query("SELECT * FROM zaznam_kontroly WHERE id = :zaznamId AND ulId = :ulId LIMIT 1")
    fun getZaznamByIdAndUlId(zaznamId: Int, ulId: Int): LiveData<ZaznamKontroly>

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