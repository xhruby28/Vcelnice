package com.hruby.databasemodule.databaseLogic.viewModel

import android.util.Log
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

    fun getMereneHodnotyByUlIdAndStanovisteID(ulId: Int, stanovisteId: Int) {
        repository.getMereneHodnotyByUlIdAndStanovisteID(ulId,stanovisteId).observeForever {
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

    fun checkAndInsertMereneHodnoty(ulId: Int, ulMacAddress: String, datum: Long, mereneHodnoty: MereneHodnoty) {
        viewModelScope.launch {
            val ul = repository.getUlByIdAndMAC(ulId, ulMacAddress)
            if (ul != null) {
                val existingRecord = repository.getMereneHodnotyByUlIdAndDate(ulId, datum)
                Log.d("checkAndInsertMereneHodnoty", "Checking for duplication with ulId: $ulId, datum: $datum")
                if (existingRecord == null) {
                    Log.d("checkAndInsertMereneHodnoty", "Inserting new record.")
                    repository.insertMereneHodnoty(mereneHodnoty)
                } else {
                    Log.d("checkAndInsertMereneHodnoty", "Received values are duplicate")
                }
            } else {
                Log.d("checkAndInsertMereneHodnoty", "Ul does not exist, cannot insert data")
            }
        }
    }
}