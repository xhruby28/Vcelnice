package com.hruby.databasemodule.databaseLogic.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hruby.databasemodule.data.Problem
import com.hruby.databasemodule.databaseLogic.repository.ProblemRepository
import kotlinx.coroutines.launch


class ProblemViewModel(private val repository: ProblemRepository) : ViewModel() {
    private val _problem = MutableLiveData<List<Problem>>()
    val problem: LiveData<List<Problem>> get() = _problem

    fun getProblemByUlId(ulId: Int) {
        repository.getProblemByUlId(ulId).observeForever {
            _problem.value = it
        }
    }

    fun insertProblem(problem: Problem) {
        viewModelScope.launch {
            repository.insertProblem(problem)
        }
    }

    fun updateProblem(problem: Problem) {
        viewModelScope.launch {
            repository.updateProblem(problem)
        }
    }

    fun deleteProblem(problem: Problem) {
        viewModelScope.launch {
            repository.deleteProblem(problem)
        }
    }
}