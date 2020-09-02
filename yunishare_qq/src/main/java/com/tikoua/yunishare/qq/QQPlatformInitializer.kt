package com.tikoua.yunishare.qq

import android.content.Context
import androidx.startup.Initializer

/**
 *   created by dcl
 *   on 2020/9/2 20:45
 */
class QQPlatformInitializer : Initializer<QQPlatform> {
    override fun create(context: Context): QQPlatform {
        return QQPlatform().apply { this.init(context) }
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}