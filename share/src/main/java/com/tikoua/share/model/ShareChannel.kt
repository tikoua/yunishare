package com.tikoua.share.model

/**
 *   created by dcl
 *   on 2020/8/12 12:35 PM
 *   分享支持的所有平台的枚举类
 */
enum class ShareChannel(val type: Int) {
    WechatFriend(1),
    WechatMoment(2),
    QQFriend(3),
    QQZone(4),
    Facebook(5),
    Twitter(6), ;

}