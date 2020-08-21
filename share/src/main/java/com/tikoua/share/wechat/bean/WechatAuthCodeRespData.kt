package com.tikoua.share.wechat.bean

import com.tencent.mm.opensdk.modelbase.BaseResp

/**
 *   created by dcl
 *   on 2020/8/20 4:18 PM
 */
data class WechatAuthCodeRespData(
    val wxEc: Int,
    val authCode: String? = null
) {
    fun isSuccess(): Boolean {
        return wxEc == BaseResp.ErrCode.ERR_OK
    }
}