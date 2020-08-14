package com.tikoua.share.wechat

import android.content.Context
import android.content.pm.PackageManager

/**
 *   created by dcl
 *   on 2020/8/14 4:35 PM
 */
fun Context.getWechatMeta(): WechatShareMeta {
    val appInfo = this.packageManager.getApplicationInfo(
        this.packageName,
        PackageManager.GET_META_DATA
    )
    val metaData = appInfo.metaData
    val appid = metaData.getString("wechat_appid")
    val appSecret = metaData.getString("wechat_secret")
    val userName = metaData.getString("wechat_user_name")
    if (appid.isNullOrEmpty() || appSecret.isNullOrEmpty()) {
        throw Exception("appid or appSecret is null")
    }
    return WechatShareMeta(appid, appSecret)
}