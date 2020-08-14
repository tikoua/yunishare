package com.tikoua.share.model

/**
 *   created by dcl
 *   on 2020/8/13 4:18 PM
 */
enum class ShareType(val type: Int) {
    Text(1),
    Image(2),
    Video(3),
    WechatMiniProgram(4),
    ;
}