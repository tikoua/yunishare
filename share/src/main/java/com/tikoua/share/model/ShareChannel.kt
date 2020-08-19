package com.tikoua.share.model

/**
 *   created by dcl
 *   on 2020/8/12 12:35 PM
 *   分享支持的所有平台的枚举类
 */
enum class ShareChannel(val type: Int) {
    System(1),
    WechatFriend(2),
    WechatMoment(3),
    QQFriend(4),
    QQZone(5),
    Facebook(6),
    Twitter(7), ;

}