package com.hruby.databasemodule.databaseLogic.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hruby.databasemodule.data.MereneHodnoty
import com.hruby.databasemodule.databaseLogic.repository.MereneHodnotyRepository
import kotlinx.coroutines.launch

class MereneHodnotyViewModel(private val repository: MereneHodnotyRepository) : ViewModel() {
    private val _mereneHodnoty = MutableLiveData<List<MereneHodnoty>>()
    val mereneHodnoty: LiveData<List<MereneHodnoty>> get() = _mereneHodnoty

    fun getMereneHodnotyByUlId(ulId: Int) {
        repository.getMereneHodnotyByUlId(ulId).observeForever {
            _mereneHodnoty.value = it
        }
    }

    fun insertMereneHodnoty(hodnoty: MereneHodnoty) {
        viewModelScope.launch {
            repository.insertMereneHodnoty(hodnoty)
        }
    }

    fun updateMereneHodnoty(hodnoty: MereneHodnoty) {
        viewModelScope.launch {
            repository.updateMereneHodnoty(hodnoty)
        }
    }

    fun deleteMereneHodnoty(hodnoty: MereneHodnoty) {
        viewModelScope.launch {
            repository.deleteMereneHodnoty(hodnoty)
        }
    }
}