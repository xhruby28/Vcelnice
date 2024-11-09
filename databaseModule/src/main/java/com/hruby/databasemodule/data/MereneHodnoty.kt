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
    val ulId: Int,
    val datum: Long, // Datum měření
    val hmotnost: Float,
    val teplotaUl: Float,
    val vlhkostModul: Float,
    val teplotaModul: Float,
    val frekvence: Float, //nebo Int
    val gyroX: Float,
    val gyroY: Float,
    val gyroZ: Float,
    val accelX: Float,
    val accelY: Float,
    val accelZ: Float
)