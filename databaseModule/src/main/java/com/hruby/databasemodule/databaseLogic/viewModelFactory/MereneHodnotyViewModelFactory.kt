package com.hruby.databasemodule.databaseLogic.viewModelFactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hruby.databasemodule.databaseLogic.repository.MereneHodnotyRepository
import com.hruby.databasemodule.databaseLogic.viewModel.MereneHodnotyViewModel

class MereneHodnotyViewModelFactory (
    private val repository: MereneHodnotyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MereneHodnotyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MereneHodnotyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}