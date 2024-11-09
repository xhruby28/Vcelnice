package com.hruby.databasemodule.databaseLogic.viewModelFactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hruby.databasemodule.databaseLogic.repository.PoznamkaRepository
import com.hruby.databasemodule.databaseLogic.viewModel.PoznamkaViewModel

class PoznamkaViewModelFactory(
    private val repository: PoznamkaRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PoznamkaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PoznamkaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}