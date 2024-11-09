package com.hruby.databasemodule.databaseLogic.repository

import androidx.lifecycle.LiveData
import com.hruby.databasemodule.data.Stanoviste
import com.hruby.databasemodule.databaseLogic.connections.StanovisteWithUly
import com.hruby.databasemodule.databaseLogic.dao.StanovisteDao

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

    fun getStanovisteWithUly(id: Int): LiveData<StanovisteWithUly> {
        return stanovisteDao.getStanovisteWithUly(id)
    }
}