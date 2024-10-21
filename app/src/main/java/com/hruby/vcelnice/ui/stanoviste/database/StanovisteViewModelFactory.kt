package com.hruby.vcelnice.ui.stanoviste.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hruby.vcelnice.ui.stanoviste.StanovisteViewModel

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