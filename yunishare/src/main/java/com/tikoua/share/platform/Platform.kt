package com.tikoua.share.platform

import android.app.Activity
import android.content.Context
import androidx.annotation.CallSuper
import com.tikoua.share.YuniShare
import com.tikoua.share.model.AuthResult
import com.tikoua.share.model.ShareChannel
import com.tikoua.share.model.ShareParams
import com.tikoua.share.model.ShareResult

/**
 *   created by dcl
 *   on 2020/8/12 12:30 PM
 */
interface Platform {
    /**
     * 各自平台的初始化操作
     *为了满足隐私政策的要求(用户同意隐私协议之后才能读写sdCard，微信的sdk初始化时会尝试读写sdCard),去掉了对jetpack startup库的使用,改为手动初始化
     */
    @CallSuper
    fun init(context: Context) {
        YuniShare.registerPlatform(this)
    }

    /**
     * 是否支持指定渠道的分享类型
     */
    fun support(type: ShareChannel): Boolean

    /**
     * 分享
     */
    suspend fun share(
        activity: Activity,
        shareChannel: ShareChannel,
        shareParams: ShareParams
    ): ShareResult

    suspend fun auth(
        activity: Activity,
        channel: ShareChannel
    ): AuthResult
}