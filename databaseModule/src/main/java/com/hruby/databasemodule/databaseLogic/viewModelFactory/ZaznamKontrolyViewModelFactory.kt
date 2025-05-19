package com.hruby.databasemodule.databaseLogic.viewModelFactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hruby.databasemodule.databaseLogic.repository.ZaznamKontrolyRepository
import com.hruby.databasemodule.databaseLogic.viewModel.ZaznamKontrolyViewModel

class ZaznamKontrolyViewModelFactory(
    private val repository: ZaznamKontrolyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ZaznamKontrolyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ZaznamKontrolyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}