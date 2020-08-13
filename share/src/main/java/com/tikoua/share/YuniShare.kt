package com.tikoua.share

import android.app.Activity
import android.content.Context
import com.tikoua.share.model.InnerShareParams
import com.tikoua.share.model.ShareChannel
import com.tikoua.share.platform.WechatPlatform

/**
 *   created by dcl
 *   on 2020/8/11 11:06 AM
 */
object YuniShare {
    private val platforms by lazy { listOf(WechatPlatform()) }
    fun init(context: Context) {
        platforms.forEach {
            it.init(context.applicationContext)
        }
    }

    fun share(activity: Activity, type: ShareChannel, shareParams: InnerShareParams) {
        val platform = platforms.firstOrNull { it.support(type) }
        platform?.share(activity, type, shareParams)
    }
}



