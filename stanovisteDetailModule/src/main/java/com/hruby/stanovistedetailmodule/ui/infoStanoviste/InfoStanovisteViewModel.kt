package com.hruby.stanovistedetailmodule.ui.infoStanoviste

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hruby.databasemodule.data.Stanoviste
import com.hruby.databasemodule.databaseLogic.repository.StanovisteRepository
import kotlinx.coroutines.launch

class InfoStanovisteViewModel(
    private val repository: StanovisteRepository,
    stanovisteId: Int
) : ViewModel() {

    val ulyCount: LiveData<Int> = repository.countUlyByStanovisteId(stanovisteId)
    val stanoviste: LiveData<Stanoviste> = repository.getStanovisteById(stanovisteId)

    fun updateStanoviste(stanoviste: Stanoviste) {
        // Spustíme aktualizaci v Repository
        viewModelScope.launch {
            repository.update(stanoviste)
        }
    }
}