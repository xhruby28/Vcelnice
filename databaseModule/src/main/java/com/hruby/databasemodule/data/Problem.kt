package com.hruby.databasemodule.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "problem",
    foreignKeys = [ForeignKey(
        entity = Uly::class,
        parentColumns = ["id"],
        childColumns = ["ulId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["ulId"])]
)
data class Problem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var ulId: Int,
    var datum: Long, // Datum problému
    var text: String
)
