package com.hruby.vcelnice.ui.stanoviste

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stanoviste")
data class Stanoviste(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var name: String?,
    //var siteMAC: String?,
    var lastCheck: String?,
    var locationUrl: String?,
    //var latitude: Float?,
    //var longitude: Float?,
    var lastState: String?,
    //var imagePath: String?,
    //val stanovisteList: MutableList<Uly> = mutableListOf(),
    var imageResId: Int = 0
)