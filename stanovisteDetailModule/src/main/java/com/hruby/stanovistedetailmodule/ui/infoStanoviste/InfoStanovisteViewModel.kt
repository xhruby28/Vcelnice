package com.hruby.stanovistedetailmodule.ui.infoStanoviste

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.hruby.databasemodule.data.Stanoviste
import com.hruby.databasemodule.databaseLogic.repository.StanovisteRepository

class InfoStanovisteViewModel(
    repository: StanovisteRepository,
    stanovisteId: Int
) : ViewModel() {

    val ulyCount: LiveData<Int> = repository.countUlyByStanovisteId(stanovisteId)

    val stanoviste: LiveData<Stanoviste> = repository.getStanovisteById(stanovisteId)
}