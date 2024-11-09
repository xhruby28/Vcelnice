package com.hruby.databasemodule.databaseLogic.viewModelFactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hruby.databasemodule.databaseLogic.repository.ProblemRepository
import com.hruby.databasemodule.databaseLogic.viewModel.ProblemViewModel

class ProblemViewModelFactory(
    private val repository: ProblemRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProblemViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProblemViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}