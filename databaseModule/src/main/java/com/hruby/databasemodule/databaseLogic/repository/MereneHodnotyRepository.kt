package com.hruby.databasemodule.databaseLogic.repository

import androidx.lifecycle.LiveData
import com.hruby.databasemodule.data.MereneHodnoty
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

    fun getMereneHodnotyByUlId(ulId: Int): LiveData<List<MereneHodnoty>> {
        return db.mereneHodnotyDao().getMereneHodnotyByUlId(ulId)
    }
}