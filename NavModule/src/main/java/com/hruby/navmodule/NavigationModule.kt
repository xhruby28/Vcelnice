package com.hruby.navmodule

import android.app.Activity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(ActivityComponent::class)
object NavigationModule {

    @Provides
    fun provideNavigator(activity: Activity): Navigator {
        return activity as? Navigator
            ?: throw IllegalStateException("Activity must implement Navigator")
    }
}