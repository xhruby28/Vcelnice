package com.hruby.databasemodule.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "poznamka",
    foreignKeys = [ForeignKey(
        entity = Uly::class,
        parentColumns = ["id"],
        childColumns = ["ulId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["ulId"])]
)
data class Poznamka(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var ulId: Int,
    var datum: Long, // Datum poznámky
    var text: String
)