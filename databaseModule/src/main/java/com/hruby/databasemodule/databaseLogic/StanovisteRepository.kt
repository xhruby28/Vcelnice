package com.hruby.databasemodule.databaseLogic

import androidx.lifecycle.LiveData
import com.hruby.databasemodule.data.Stanoviste

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

    fun getStanovisteById(id: Int): LiveData<Stanoviste> {
        return stanovisteDao.getStanovisteById(id)
    }
}