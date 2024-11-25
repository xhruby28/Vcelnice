package com.hruby.databasemodule.databaseLogic.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hruby.databasemodule.data.Stanoviste
import com.hruby.databasemodule.databaseLogic.repository.StanovisteRepository
import kotlinx.coroutines.launch

class StanovisteViewModel(private val repository: StanovisteRepository) : ViewModel()  {

    //private val repository: StanovisteRepository
    val allStanoviste: LiveData<List<Stanoviste>> = repository.allStanoviste

    // Přidání nového stanoviště
    fun insertStanoviste(stanoviste: Stanoviste) {
        viewModelScope.launch {
            repository.insert(stanoviste)
        }
    }

    // Aktualizace stanoviště
    fun updateStanoviste(stanoviste: Stanoviste) {
        viewModelScope.launch {
            repository.update(stanoviste)
        }
    }

    // Odstranění stanoviště
    fun deleteStanoviste(stanoviste: Stanoviste) {
        viewModelScope.launch {
            repository.delete(stanoviste)
        }
    }

    // Získání stanovistě podle ID
    fun getStanovisteById(id: Int): LiveData<Stanoviste> {
        return repository.getStanovisteById(id)
    }

    fun getStanovisteByMAC(macAddress: String): LiveData<Stanoviste>{
        return repository.getStanovisteByMAC(macAddress)
    }

    fun countUlyByStanovisteId(stanovisteId: Int): LiveData<Int> {
        return repository.countUlyByStanovisteId(stanovisteId)
    }
}