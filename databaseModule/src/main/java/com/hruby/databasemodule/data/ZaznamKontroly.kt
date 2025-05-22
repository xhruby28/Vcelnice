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
    var problemovyUl: Boolean = false,
    var problemText: String? = null,

    // Matka
    var matkaVidena: Boolean = false,
    var matkaBarva: String? = null,
    var matkaRok: String?= null,
    var matkaZastrizeneKridla: Boolean = false,
    var matkaDatumVidena: Long? = null,

    // Zasahy
    var zasahMatkaOznacena: Boolean = false,
    var zasahKrmeni: Boolean = false,
    var zasahVlozenaMrizka: Boolean = false,
    var zasahOdebranaMrizka: Boolean = false,
    var zasahPripravaNaKrmeni: Boolean = false,
    var zasahZrusenoSpojenim: Boolean = false,
    var zasahVycisteneDno: Boolean = false,
    var zasahOskrabaneSteny: Boolean = false,

    // Nástavky
    var nastavekAkce: String? = null, // "Přidán", "Odebrán", "Prohozen", "Vyměněn"

    // Mezistěny a souše
    var mezistenyPlodiste: Int = 0,
    var souskyPlodiste: Int = 0,
    var mezistenyMednik: Int = 0,
    var souskyMednik: Int = 0,

    // Plod
    var plodZavreny: Boolean = false,
    var plodZavrenyPocet: Int? = null,
    var plodOtevreny: Boolean = false,
    var plodOtevrenyPocet: Int? = null,
    var plodVMedniku: Boolean = false,
    var plodVMednikuPocet: Int? = null,
    var plodTrubcina: Boolean = false,
    var plodTrubcinaPocet: Int? = null,
    var plodMatecnik: Boolean = false,
    var plodMatecnikPocet: Int? = null,

    var hodnoceni: Float = 0.0f, // Hodnocení pomocí hvězdiček

    var agresivita: Int = 0,
    var mezolitostPlodu: Int = 0,
    var silaVcelstva: Int = 0,
    var stavebniPud: Int = 0
) : Serializable
