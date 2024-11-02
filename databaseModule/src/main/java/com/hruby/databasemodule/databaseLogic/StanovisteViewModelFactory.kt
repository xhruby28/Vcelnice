package com.hruby.databasemodule.databaseLogic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

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