package com.tikoua.share.wechat

import android.content.Context
import androidx.startup.Initializer

/**
 *   created by dcl
 *   on 2020/9/2 20:45
 */
class WechatPlatformInitializer : Initializer<WechatPlatform> {
    override fun create(context: Context): WechatPlatform {
        return WechatPlatform().apply { this.init(context) }
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }
}