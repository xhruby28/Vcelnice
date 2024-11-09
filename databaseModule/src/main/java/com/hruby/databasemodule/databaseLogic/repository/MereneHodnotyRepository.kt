package com.hruby.databasemodule.databaseLogic.repository

import androidx.lifecycle.LiveData
import com.hruby.databasemodule.data.MereneHodnoty
import com.hruby.databasemodule.databaseLogic.dao.MereneHodnotyDao

class MereneHodnotyRepository(private val mereneHodnotyDao: MereneHodnotyDao) {
    suspend fun insertMereneHodnoty(hodnoty: MereneHodnoty) {
        mereneHodnotyDao.insertMereneHodnoty(hodnoty)
    }

    suspend fun updateMereneHodnoty(hodnoty: MereneHodnoty) {
        mereneHodnotyDao.updateMereneHodnoty(hodnoty)
    }

    suspend fun deleteMereneHodnoty(hodnoty: MereneHodnoty) {
        mereneHodnotyDao.deleteMereneHodnoty(hodnoty)
    }

    fun getMereneHodnotyByUlId(ulId: Int): LiveData<List<MereneHodnoty>> {
        return mereneHodnotyDao.getMereneHodnotyByUlId(ulId)
    }
}