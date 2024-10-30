package com.hruby.vcelnice.ui.stanoviste

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stanoviste")
data class Stanoviste(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var name: String?,
    var siteMAC: String?,
    var lastCheck: String?,
    var locationUrl: String?,
    var lastState: String?,
    //var imagePath: String?,
    //val hiveList: MutableList<Hives> = mutableListOf(),
    var imageResId: Int = 0
)