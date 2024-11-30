package com.hruby.databasemodule.databaseLogic.repository

import androidx.lifecycle.LiveData
import com.hruby.databasemodule.data.Stanoviste
import com.hruby.databasemodule.data.Uly
import com.hruby.databasemodule.databaseLogic.StanovisteDatabase
import com.hruby.databasemodule.databaseLogic.connections.UlWithOther

class UlyRepository(private val db: StanovisteDatabase) {
    suspend fun insertUl(ul: Uly) {
        db.ulyDao().insertUl(ul)
    }

    suspend fun updateUl(ul: Uly) {
        db.ulyDao().updateUl(ul)
    }

    suspend fun deleteUl(ul: Uly) {
        db.ulyDao().deleteUl(ul)
    }

    fun getUlyByStanovisteId(stanovisteId: Int): LiveData<List<Uly>> {
        return db.ulyDao().getUlyByStanovisteId(stanovisteId)
    }

    fun getUlWithOthersByStanovisteId(ulId: Int, stanovisteId: Int): LiveData<UlWithOther> {
        return db.ulyDao().getUlWithOthersByStanovisteId(ulId, stanovisteId)
    }

    suspend fun getUlWithOthersByMACAndStanovisteMAC(macAddress: String, stanovisteMac: String): Uly? {
        return db.ulyDao().getUlWithOthersByMACAndStanovisteMAC(macAddress, stanovisteMac)
    }
}