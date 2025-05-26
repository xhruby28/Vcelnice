package com.hruby.databasemodule.databaseLogic.repository

import androidx.lifecycle.LiveData
import com.hruby.databasemodule.data.ZaznamKontroly
import com.hruby.databasemodule.databaseLogic.StanovisteDatabase

class ZaznamKontrolyRepository(private val db: StanovisteDatabase) {
    suspend fun insertZaznam(zaznam: ZaznamKontroly) {
        db.zaznamKontrolyDao().insertZaznam(zaznam)
    }

    suspend fun updateZaznam(zaznam: ZaznamKontroly) {
        db.zaznamKontrolyDao().updateZaznam(zaznam)
    }

    suspend fun deleteZaznam(zaznam: ZaznamKontroly) {
        db.zaznamKontrolyDao().deleteZaznam(zaznam)
    }

    fun getZaznamyByUlId(ulId: Int): LiveData<List<ZaznamKontroly>> {
        return db.zaznamKontrolyDao().getZaznamyByUlId(ulId)
    }

    fun getLastZaznamForUl(ulId: Int): LiveData<ZaznamKontroly?> {
        return db.zaznamKontrolyDao().getLastZaznamForUl(ulId)
    }

    fun getLastMatkaVidenaByUlId(ulId: Int): LiveData<ZaznamKontroly?> {
        return db.zaznamKontrolyDao().getLastMatkaVidenaByUlId(ulId)
    }

    fun getZaznamByIdAndUlId(zaznamId: Int, ulId: Int): LiveData<ZaznamKontroly> {
        return db.zaznamKontrolyDao().getZaznamByIdAndUlId(zaznamId, ulId)
    }

    fun getLastZaznamyByStanovisteId(stanovisteId: Int): LiveData<List<ZaznamKontroly>> {
        return db.zaznamKontrolyDao().getLastZaznamyByStanovisteId(stanovisteId)
    }
}