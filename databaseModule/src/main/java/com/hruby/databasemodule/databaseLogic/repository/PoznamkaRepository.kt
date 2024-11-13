package com.hruby.databasemodule.databaseLogic.repository

import androidx.lifecycle.LiveData
import com.hruby.databasemodule.data.Poznamka
import com.hruby.databasemodule.databaseLogic.StanovisteDatabase

class PoznamkaRepository(private val db: StanovisteDatabase) {
    suspend fun insertPoznamka(poznamka: Poznamka) {
        db.poznamkaDao().insertPoznamka(poznamka)
    }

    suspend fun updatePoznamka(poznamka: Poznamka) {
        db.poznamkaDao().updatePoznamka(poznamka)
    }

    suspend fun deletePoznamka(poznamka: Poznamka) {
        db.poznamkaDao().deletePoznamka(poznamka)
    }

    fun getPoznamkyByUlId(ulId: Int): LiveData<List<Poznamka>> {
        return db.poznamkaDao().getPoznamkyByUlId(ulId)
    }
}