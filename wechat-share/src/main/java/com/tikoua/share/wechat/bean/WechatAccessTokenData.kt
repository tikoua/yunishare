package com.tikoua.share.wechat.bean

/**
 *   created by dcl
 *   on 2020/8/20 8:17 PM
 */
data class WechatAccessTokenData(
    var accessToken: String,
    var expiresIn: String,
    var refreshToken: String,
    var openid: String,
    var scope: String,
    var unionid: String,
    var errcode: Int?,
    var errmsg: String? = null
)