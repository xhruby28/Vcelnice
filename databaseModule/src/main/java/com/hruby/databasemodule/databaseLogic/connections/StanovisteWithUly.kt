package com.hruby.databasemodule.databaseLogic.connections

import androidx.room.Embedded
import androidx.room.Relation
import com.hruby.databasemodule.data.Stanoviste
import com.hruby.databasemodule.data.Uly

data class StanovisteWithUly(
    @Embedded val stanoviste: Stanoviste,
    @Relation(
        parentColumn = "id",
        entityColumn = "stanovisteId"
    )
    val uly: List<Uly>
)
