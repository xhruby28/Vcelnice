package com.hruby.databasemodule.databaseLogic.viewModelFactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hruby.databasemodule.databaseLogic.repository.UlyRepository
import com.hruby.databasemodule.databaseLogic.viewModel.UlyViewModel

class UlyViewModelFactory(
    private val repository: UlyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UlyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UlyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}