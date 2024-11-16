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
    val stanovisteId: Int, // Možná změnit na var stanovisteId, kdyby chtěl včelař přesunout ul z jednoho stanoviště na druhé
    var cisloUlu: Int?,
    var popis: String?,
    var maMAC: Boolean = false,
    var macAddress: String? = null,
    var problemovyUl: Boolean = false,
    var posledniProblem: String? = null, // Poslední zaznamenaný problém
    var hodnoceni: Float = 0.0f, // Hodnocení pomocí hvězdiček
    var agresivita: Int = 0,
    var stavZasob: String? = null,
    var mezolitostPlodu: Int = 0,
    var silaVcelstva: Int = 0,
    var stavebniPud: Int = 0
)
