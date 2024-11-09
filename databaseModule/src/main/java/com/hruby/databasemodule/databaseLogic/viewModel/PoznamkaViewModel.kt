package com.hruby.databasemodule.databaseLogic.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hruby.databasemodule.data.Poznamka
import com.hruby.databasemodule.databaseLogic.repository.PoznamkaRepository
import kotlinx.coroutines.launch

class PoznamkaViewModel(private val repository: PoznamkaRepository) : ViewModel() {
    private val _poznamky = MutableLiveData<List<Poznamka>>()
    val poznamky: LiveData<List<Poznamka>> get() = _poznamky

    fun getPoznamkyByUlId(ulId: Int) {
        repository.getPoznamkyByUlId(ulId).observeForever {
            _poznamky.value = it
        }
    }

    fun insertPoznamka(poznamka: Poznamka) {
        viewModelScope.launch {
            repository.insertPoznamka(poznamka)
        }
    }

    fun updatePoznamka(poznamka: Poznamka) {
        viewModelScope.launch {
            repository.updatePoznamka(poznamka)
        }
    }

    fun deletePoznamka(poznamka: Poznamka) {
        viewModelScope.launch {
            repository.deletePoznamka(poznamka)
        }
    }
}