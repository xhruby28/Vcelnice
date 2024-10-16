package com.hruby.vcelnice.ui.stanoviste

data class Stanoviste(var name: String?,
                        var lastCheck: String?,
                        var locationUrl: String?,
                        //var latitude: Float?,
                        //var longitude: Float?,
                        var lastState: String?,
                        var imageResId: Int = 0)