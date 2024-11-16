package com.hruby.stanovistedetailmodule.ui.infoStanoviste

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hruby.databasemodule.databaseLogic.StanovisteDatabase
import com.hruby.databasemodule.databaseLogic.repository.StanovisteRepository

class InfoStanovisteViewModelFactory(
    private val context: Context,
    private val stanovisteId: Int
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InfoStanovisteViewModel::class.java)) {
            val database = StanovisteDatabase.getDatabase(context)
            val repository = StanovisteRepository(database)
            @Suppress("UNCHECKED_CAST")
            return InfoStanovisteViewModel(repository, stanovisteId) as T
        }
        throw IllegalArgumentException("Neznámý ViewModel class")
    }
}