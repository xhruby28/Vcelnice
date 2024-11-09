package com.hruby.databasemodule.databaseLogic.repository

import androidx.lifecycle.LiveData
import com.hruby.databasemodule.data.Uly
import com.hruby.databasemodule.databaseLogic.connections.UlWithOther
import com.hruby.databasemodule.databaseLogic.dao.UlyDao

class UlyRepository(private val ulyDao: UlyDao) {
    suspend fun insertUl(ul: Uly) {
        ulyDao.insertUl(ul)
    }

    suspend fun updateUl(ul: Uly) {
        ulyDao.updateUl(ul)
    }

    suspend fun deleteUl(ul: Uly) {
        ulyDao.deleteUl(ul)
    }

    fun getUlyByStanovisteId(stanovisteId: Int): LiveData<List<Uly>> {
        return ulyDao.getUlyByStanovisteId(stanovisteId)
    }

    fun getUlWithOthersByStanovisteId(stanovisteId: Int): LiveData<List<UlWithOther>> {
        return ulyDao.getUlWithOthersByStanovisteId(stanovisteId)
    }
}