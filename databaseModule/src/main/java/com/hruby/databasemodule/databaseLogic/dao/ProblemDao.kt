package com.hruby.databasemodule.databaseLogic.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hruby.databasemodule.data.Problem

@Dao
interface ProblemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProblem(problem: Problem)

    @Update
    suspend fun updateProblem(problem: Problem)

    @Delete
    suspend fun deleteProblem(problem: Problem)

    @Query("SELECT * FROM problem WHERE ulId = :ulId ORDER BY datum DESC")
    fun getProblemByUlId(ulId: Int): LiveData<List<Problem>>
}