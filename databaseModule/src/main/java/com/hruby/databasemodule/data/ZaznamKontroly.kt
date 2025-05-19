package com.hruby.databasemodule.data


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
    tableName = "zaznam_kontroly",
    foreignKeys = [ForeignKey(
        entity = Uly::class,
        parentColumns = ["id"],
        childColumns = ["ulId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["ulId"])]
)
data class ZaznamKontroly(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ulId: Int,
    val datum: Long, // Timestamp
    var typKontroly: String? = null,
    var zaznamText: String? = null,
    var stavZasob: String? = null,
    var medobrani: Boolean = false,
    var medobraniRamky: Int? = null,
    var problemovyUl: Boolean = false,
    var hodnoceni: Float = 0.0f, // Hodnocení pomocí hvězdiček
    var agresivita: Int = 0,
    var mezolitostPlodu: Int = 0,
    var silaVcelstva: Int = 0,
    var stavebniPud: Int = 0
) : Serializable
