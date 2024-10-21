package com.hruby.vcelnice.ui.stanoviste

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hruby.vcelnice.ui.stanoviste.database.StanovisteRepository
import kotlinx.coroutines.launch

class StanovisteViewModel(private val repository: StanovisteRepository) : ViewModel()  {

    //private val repository: StanovisteRepository
    val allStanoviste: LiveData<List<Stanoviste>> = repository.allStanoviste

//    init {
//        val stanovisteDao = StanovisteDatabase.getDatabase(application).stanovisteDao()
//        repository = StanovisteRepository(stanovisteDao)
//        allStanoviste = repository.allStanoviste // Všechny stanoviště
//    }

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
}