package com.hruby.vcelnice.ui.stanoviste.database

import androidx.lifecycle.LiveData
import com.hruby.vcelnice.ui.stanoviste.Stanoviste

class StanovisteRepository(private val stanovisteDao: StanovisteDao) {

    val allStanoviste: LiveData<List<Stanoviste>> = stanovisteDao.getAllStanoviste()

    suspend fun insert(stanoviste: Stanoviste) {
        stanovisteDao.insert(stanoviste)
    }

    suspend fun update(stanoviste: Stanoviste) {
        stanovisteDao.update(stanoviste)
    }

    suspend fun delete(stanoviste: Stanoviste) {
        stanovisteDao.delete(stanoviste)
    }
}