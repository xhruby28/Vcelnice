package com.hruby.databasemodule.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "stanoviste")
data class Stanoviste(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var name: String?,
    var maMAC: Boolean = false,
    var siteMAC: String? = null,
    var lastCheck: String?,
    var locationUrl: String?,
    var lastState: String?,
    var imagePath: String? = null,

    // Nové sloupce
    var notificationsEnabled: Boolean = false,
    var notificationPhoneNumber: String? = null,
    var isPin: Boolean = false,
    var hashedPin: String? = null,

    var description: String? = null,
) : Serializable