package com.hruby.databasemodule.data

import androidx.room.PrimaryKey

data class Hives(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var hiveId: Int,
    var aggressiveness: Int,                                // Agresivita včel/bodavost
    var problemHive: Boolean,                               // Problémový úl - ukázat v dashboardu všech úlů na stanovišti
    //var problems: MutableList<Problems> = mutableListOf() //	Poslední zaznamenaný problém s úlem
    var totalRating: Int,                                   //	Značení pomocí hvězdiček - mohlo by být i vidět v dashboardu


    //	Stav zásob
    //	Mezolitost plodu
    //	Síla včelstva
    //	Stavební pud
    //  Nejnovější poznámka

)
