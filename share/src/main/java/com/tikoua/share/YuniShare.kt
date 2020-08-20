package com.tikoua.share

import android.app.Activity
import android.content.Context
import com.tikoua.share.model.*
import com.tikoua.share.platform.*

/**
 *   created by dcl
 *   on 2020/8/11 11:06 AM
 */
object YuniShare {
    private val platforms by lazy {
        listOf(
            WechatPlatform(),
            QQPlatform(),
            FacebookPlatform(),
            TwitterPlatform(),
            SystemPlatform()
        )
    }

    fun init(context: Context) {
        platforms.forEach {
            it.init(context.applicationContext)
        }
    }

    suspend fun share(
        activity: Activity,
        channel: ShareChannel,
        shareParams: InnerShareParams
    ): ShareResult {
        val platform =
            platforms.firstOrNull { it.support(channel) }
                ?: return ShareResult(ShareEc.Unsupported)
        return platform.share(activity, channel, shareParams)
    }

    suspend fun auth(
        activity: Activity,
        channel: ShareChannel
    ): AuthResult {
        val platform =
            platforms.firstOrNull { it.support(channel) }
                ?: return AuthResult(ShareEc.Unsupported)
        return platform.auth(activity, channel)
    }
}



