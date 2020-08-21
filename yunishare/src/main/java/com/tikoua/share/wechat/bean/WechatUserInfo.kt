package com.tikoua.share.wechat.bean

import org.json.JSONArray

/**
 *   created by dcl
 *   on 2020/8/21 12:06 PM
 */
data class WechatUserInfo(
    //普通用户的标识，对当前开发者帐号唯一
    val openid: String?,
    //普通用户昵称
    val nickname: String?,
    //普通用户性别，1 为男性，2 为女性
    val sex: Int?,
    //普通用户个人资料填写的省份
    val language: String?,
    //普通用户个人资料填写的城市
    val city: String?,
    //普通用户个人资料填写的省份
    val province: String?,
    //国家，如中国为 CN
    val country: String?,
    //用户头像，最后一个数值代表正方形头像大小（有 0、46、64、96、132 数值可选，0 代表 640*640 正方形头像），用户没有头像时该项为空
    val headimgurl: String?,
    //用户特权信息，json 数组，如微信沃卡用户为（chinaunicom）
    val privilege: JSONArray?,
    //用户统一标识。针对一个微信开放平台帐号下的应用，同一用户的 unionid 是唯一的。
    val unionid: String?,
    //获取失败时会返回错误码和错误信息
    val errcode: Int? = null,
    //获取失败时会返回错误码和错误信息
    val errmsg: String? = null
) {
}