package com.hruby.databasemodule.databaseLogic.repository

import androidx.lifecycle.LiveData
import com.hruby.databasemodule.data.Stanoviste
import com.hruby.databasemodule.databaseLogic.StanovisteDatabase
import com.hruby.databasemodule.databaseLogic.connections.StanovisteWithUly

class StanovisteRepository(private val db: StanovisteDatabase) {

    val allStanoviste: LiveData<List<Stanoviste>> = db.stanovisteDao().getAllStanoviste()

    suspend fun insert(stanoviste: Stanoviste) {
        db.stanovisteDao().insert(stanoviste)
    }

    suspend fun update(stanoviste: Stanoviste) {
        db.stanovisteDao().update(stanoviste)
    }

    suspend fun delete(stanoviste: Stanoviste) {
        db.stanovisteDao().delete(stanoviste)
    }

    fun getStanovisteById(id: Int): LiveData<Stanoviste> {
        return db.stanovisteDao().getStanovisteById(id)
    }

    fun getStanovisteWithUly(id: Int): LiveData<StanovisteWithUly> {
        return db.stanovisteDao().getStanovisteWithUly(id)
    }

    fun getStanovisteByMAC(macAddress: String): Stanoviste? {
        return db.stanovisteDao().getStanovisteByMAC(macAddress)
    }

    fun countUlyByStanovisteId(stanovisteId: Int): LiveData<Int> {
        return db.ulyDao().countUlyByStanovisteId(stanovisteId)
    }
}