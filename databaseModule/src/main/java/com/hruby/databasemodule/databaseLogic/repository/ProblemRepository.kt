package com.hruby.databasemodule.databaseLogic.repository

import androidx.lifecycle.LiveData
import com.hruby.databasemodule.data.Problem
import com.hruby.databasemodule.databaseLogic.StanovisteDatabase

class ProblemRepository(private val db: StanovisteDatabase) {
    suspend fun insertProblem(problem: Problem) {
        db.problemDao().insertProblem(problem)
    }

    suspend fun updateProblem(problem: Problem) {
        db.problemDao().updateProblem(problem)
    }

    suspend fun deleteProblem(problem: Problem) {
        db.problemDao().deleteProblem(problem)
    }

    fun getProblemByUlId(ulId: Int): LiveData<List<Problem>> {
        return db.problemDao().getProblemByUlId(ulId)
    }
}