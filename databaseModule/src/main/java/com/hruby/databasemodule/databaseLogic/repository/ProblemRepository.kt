package com.hruby.databasemodule.databaseLogic.repository

import androidx.lifecycle.LiveData
import com.hruby.databasemodule.data.Problem
import com.hruby.databasemodule.databaseLogic.dao.ProblemDao

class ProblemRepository(private val problemDao: ProblemDao) {
    suspend fun insertProblem(problem: Problem) {
        problemDao.insertProblem(problem)
    }

    suspend fun updateProblem(problem: Problem) {
        problemDao.updateProblem(problem)
    }

    suspend fun deleteProblem(problem: Problem) {
        problemDao.deleteProblem(problem)
    }

    fun getProblemByUlId(ulId: Int): LiveData<List<Problem>> {
        return problemDao.getProblemByUlId(ulId)
    }
}