package com.hruby.databasemodule.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "uly",
    foreignKeys = [ForeignKey(
        entity = Stanoviste::class,
        parentColumns = ["id"],
        childColumns = ["stanovisteId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["stanovisteId"])]
)
data class Uly(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val stanovisteId: Int,
    val nazev: String,
    val popis: String,
    val problemovyUl: Boolean,
    val posledniProblem: String?, // Poslední zaznamenaný problém
    val hodnoceni: Int, // Hodnocení pomocí hvězdiček
    val agresivita: Int,
    val stavZasob: String,
    val mezolitostPlodu: Int,
    val silaVcelstva: Int,
    val stavebniPud: Int
)
