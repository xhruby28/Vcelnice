package com.hruby.databasemodule.databaseLogic.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hruby.databasemodule.data.ZaznamKontroly
import com.hruby.databasemodule.databaseLogic.repository.ZaznamKontrolyRepository
import kotlinx.coroutines.launch

class ZaznamKontrolyViewModel(private val repository: ZaznamKontrolyRepository) : ViewModel() {
    fun getZaznamyByUlId(ulId: Int): LiveData<List<ZaznamKontroly>> {
        return repository.getZaznamyByUlId(ulId)
    }

    fun getLastZaznamyByStanovisteId(stanovisteId: Int): LiveData<List<ZaznamKontroly>> {
        return repository.getLastZaznamyByStanovisteId(stanovisteId)
    }

    fun insertZaznam(zaznam: ZaznamKontroly) {
        viewModelScope.launch {
            repository.insertZaznam(zaznam)
        }
    }

    fun updateZaznam(zaznam: ZaznamKontroly) {
        viewModelScope.launch {
            repository.updateZaznam(zaznam)
        }
    }

    fun deleteZaznam(zaznam: ZaznamKontroly) {
        viewModelScope.launch {
            repository.deleteZaznam(zaznam)
        }
    }
}