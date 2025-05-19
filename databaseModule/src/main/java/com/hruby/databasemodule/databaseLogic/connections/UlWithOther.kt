package com.hruby.databasemodule.databaseLogic.connections

import androidx.room.Embedded
import androidx.room.Relation
import com.hruby.databasemodule.data.MereneHodnoty
import com.hruby.databasemodule.data.Poznamka
import com.hruby.databasemodule.data.Problem
import com.hruby.databasemodule.data.Uly
import com.hruby.databasemodule.data.ZaznamKontroly

data class UlWithOther(
    @Embedded val ul: Uly,
    @Relation(
        parentColumn = "id",
        entityColumn = "ulId"
    )
    val poznamky: List<Poznamka>,
    @Relation(
        parentColumn = "id",
        entityColumn = "ulId"
    )
    val mereneHodnoty: List<MereneHodnoty>,
    @Relation(
        parentColumn = "id",
        entityColumn = "ulId"
    )
    val problem: List<Problem>,
    @Relation(
        parentColumn = "id",
        entityColumn = "ulId"
    )
    val zaznamKontroly: List<ZaznamKontroly>
)