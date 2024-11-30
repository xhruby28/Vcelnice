package com.hruby.databasemodule.databaseLogic.repository

import androidx.lifecycle.LiveData
import com.hruby.databasemodule.data.MereneHodnoty
import com.hruby.databasemodule.data.Uly
import com.hruby.databasemodule.databaseLogic.StanovisteDatabase

class MereneHodnotyRepository(private val db: StanovisteDatabase) {
    suspend fun insertMereneHodnoty(hodnoty: MereneHodnoty) {
        db.mereneHodnotyDao().insertMereneHodnoty(hodnoty)
    }

    suspend fun updateMereneHodnoty(hodnoty: MereneHodnoty) {
        db.mereneHodnotyDao().updateMereneHodnoty(hodnoty)
    }

    suspend fun deleteMereneHodnoty(hodnoty: MereneHodnoty) {
        db.mereneHodnotyDao().deleteMereneHodnoty(hodnoty)
    }

    fun getMereneHodnotyByUlIdAndStanovisteID(ulId: Int, stanovisteId: Int): LiveData<List<MereneHodnoty>> {
        return db.mereneHodnotyDao().getMereneHodnotyByUlIdAndStanovisteID(ulId,stanovisteId)
    }

    suspend fun getMereneHodnotyByUlIdAndDate(ulId: Int, datum: Long): MereneHodnoty? {
        return db.mereneHodnotyDao().getMereneHodnotyByUlIdAndDate(ulId,datum)
    }

    suspend fun getUlByIdAndMAC(ulId: Int, mac: String): Uly? {
        return db.mereneHodnotyDao().getUlByIdAndMAC(ulId,mac)
    }
}