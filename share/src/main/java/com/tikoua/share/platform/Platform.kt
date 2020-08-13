package com.tikoua.share.platform

import android.app.Activity
import android.content.Context
import com.tikoua.share.model.InnerShareParams
import com.tikoua.share.model.ShareChannel

/**
 *   created by dcl
 *   on 2020/8/12 12:30 PM
 */
interface Platform {
    /**
     * 各自平台的初始化操作
     */
    fun init(context: Context)

    /**
     * 是否支持指定渠道的分享类型
     */
    fun support(type: ShareChannel): Boolean

    /**
     * 分享
     */
    fun share(activity: Activity, type: ShareChannel, shareParams: InnerShareParams)
}