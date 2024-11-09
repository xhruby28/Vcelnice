package com.hruby.databasemodule.databaseLogic.repository

import androidx.lifecycle.LiveData
import com.hruby.databasemodule.data.Poznamka
import com.hruby.databasemodule.databaseLogic.dao.PoznamkaDao

class PoznamkaRepository(private val poznamkaDao: PoznamkaDao) {
    suspend fun insertPoznamka(poznamka: Poznamka) {
        poznamkaDao.insertPoznamka(poznamka)
    }

    suspend fun updatePoznamka(poznamka: Poznamka) {
        poznamkaDao.updatePoznamka(poznamka)
    }

    suspend fun deletePoznamka(poznamka: Poznamka) {
        poznamkaDao.deletePoznamka(poznamka)
    }

    fun getPoznamkyByUlId(ulId: Int): LiveData<List<Poznamka>> {
        return poznamkaDao.getPoznamkyByUlId(ulId)
    }
}