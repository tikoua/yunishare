package com.tikoua.share

import android.app.Activity
import android.content.Context
import com.tikoua.share.model.InnerShareParams
import com.tikoua.share.model.ShareChannel
import com.tikoua.share.model.ShareEc
import com.tikoua.share.model.ShareResult
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
        type: ShareChannel,
        shareParams: InnerShareParams
    ): ShareResult {
        val platform =
            platforms.firstOrNull { it.support(type) }
                ?: return ShareResult(ShareEc.Unsupported)
        return platform.share(activity, type, shareParams)
    }
}



