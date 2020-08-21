package com.tikoua.share.qq

import android.content.Context
import android.content.pm.PackageManager
import com.tikoua.share.utils.log

/**
 *   created by dcl
 *   on 2020/8/17 4:30 PM
 */

fun Context.loadQQMeta(): QQShareMeta {
    val appInfo = this.packageManager.getApplicationInfo(
        this.packageName,
        PackageManager.GET_META_DATA
    )
    val metaData = appInfo.metaData
    val appid = metaData.getString("qq_appid")
    val appKey = metaData.getString("qq_app_key")
    log("appid: $appid   appKey: $appKey")
    if (appid.isNullOrEmpty() || appKey.isNullOrEmpty()) {
        throw Exception("appid or appKey is null")
    }
    return QQShareMeta(appid, appKey)
}