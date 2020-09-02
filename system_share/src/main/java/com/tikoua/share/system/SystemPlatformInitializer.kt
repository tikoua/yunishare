package com.tikoua.share.system

import android.content.Context
import androidx.startup.Initializer

/**
 *   created by dcl
 *   on 2020/9/2 20:45
 */
class SystemPlatformInitializer : Initializer<SystemPlatform> {
    override fun create(context: Context): SystemPlatform {
        return SystemPlatform().apply { this.init(context) }
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}