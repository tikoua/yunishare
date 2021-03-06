package com.tikoua.share.wechat

import android.content.Context
import android.content.pm.PackageManager
import com.tikoua.share.model.ShareParams

/**
 *   created by dcl
 *   on 2020/8/14 4:35 PM
 */
fun Context.loadWechatMeta(): WechatShareMeta {
    val appInfo = this.packageManager.getApplicationInfo(
        this.packageName,
        PackageManager.GET_META_DATA
    )
    val metaData = appInfo.metaData
    val appid = metaData.getString("wechat_appid")
    val appSecret = metaData.getString("wechat_secret")
    if (appid.isNullOrEmpty() || appSecret.isNullOrEmpty()) {
        throw Exception("appid or appSecret  is null")
    }
    return WechatShareMeta(appid, appSecret)
}

fun ShareParams.Companion.buildWechatText(): WechatTextBuilder {
    return WechatTextBuilder()
}

fun ShareParams.Companion.buildWechatImage(): WechatImageBuilder {
    return WechatImageBuilder()
}

fun ShareParams.Companion.buildWechatVideo(): WechatVideoBuilder {
    return WechatVideoBuilder()
}

fun ShareParams.Companion.buildMiniProgram(): WechatMiniProgramBuilder {
    return WechatMiniProgramBuilder()
}

fun ShareParams.Companion.buildWechatLink(): WechatLinkBuilder {
    return WechatLinkBuilder()
}