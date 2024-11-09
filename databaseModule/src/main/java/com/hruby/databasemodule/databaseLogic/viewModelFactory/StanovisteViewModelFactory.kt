package com.hruby.databasemodule.databaseLogic.viewModelFactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hruby.databasemodule.databaseLogic.repository.StanovisteRepository
import com.hruby.databasemodule.databaseLogic.viewModel.StanovisteViewModel

class StanovisteViewModelFactory(
    private val repository: StanovisteRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StanovisteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StanovisteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}