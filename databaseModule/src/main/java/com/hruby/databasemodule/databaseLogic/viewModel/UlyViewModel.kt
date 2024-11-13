package com.hruby.databasemodule.databaseLogic.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hruby.databasemodule.data.Uly
import com.hruby.databasemodule.databaseLogic.connections.UlWithOther
import com.hruby.databasemodule.databaseLogic.repository.UlyRepository
import kotlinx.coroutines.launch

class UlyViewModel(private val repository: UlyRepository) : ViewModel() {
    private val _uly = MutableLiveData<List<Uly>>()
    private val _ulyWithOthers = MutableLiveData<List<UlWithOther>>()
    val uly: LiveData<List<Uly>> get() = _uly
    val ulyWithOthers: LiveData<List<UlWithOther>> get() = _ulyWithOthers

    fun getUlyByStanovisteId(stanovisteId: Int): LiveData<List<Uly>> {
        return repository.getUlyByStanovisteId(stanovisteId)
    }

    fun getUlWithOthersByStanovisteId(stanovisteId: Int): LiveData<List<UlWithOther>> {
        return repository.getUlWithOthersByStanovisteId(stanovisteId)
    }

    fun insertUl(ul: Uly) {
        viewModelScope.launch {
            repository.insertUl(ul)
        }
    }

    fun updateUl(ul: Uly) {
        viewModelScope.launch {
            repository.updateUl(ul)
        }
    }

    fun deleteUl(ul: Uly) {
        viewModelScope.launch {
            repository.deleteUl(ul)
        }
    }
}