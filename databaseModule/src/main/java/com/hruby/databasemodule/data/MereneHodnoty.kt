package com.hruby.databasemodule.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "merene_hodnoty",
    foreignKeys = [ForeignKey(
        entity = Uly::class,
        parentColumns = ["id"],
        childColumns = ["ulId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["ulId"])]
)
data class MereneHodnoty(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var ulId: Int,
    var datum: Long, // Datum měření
    var hmotnost: Float,
    var teplotaUl: Float,
    var vlhkostModul: Float,
    var teplotaModul: Float,
    var frekvence: Float, //nebo Int
    var gyroX: Float,
    var gyroY: Float,
    var gyroZ: Float,
    var accelX: Float,
    var accelY: Float,
    var accelZ: Float
)