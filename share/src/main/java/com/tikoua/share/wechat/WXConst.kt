package com.tikoua.share.wechat

/**
 *   created by dcl
 *   on 2020/8/14 3:00 PM
 */
object WXConst {
    const val ActionWXResp = "com.tikoua.share.wechat.action.wx.resp"
    const val WXRespDataKey = "wx_resp_data"

    /*star : 微信返回的数据的key*/
    const val RespKeyAuthState = "_wxapi_sendauth_resp_state"
    const val RespKeyAuthToken = "_wxapi_sendauth_resp_token"
    const val RespKeyTransaction = "_wxapi_baseresp_transaction"
    const val RespKeyCommandType = "_wxapi_command_type"
    const val RespKeyAuthResult = "_wxapi_sendauth_resp_auth_result"
    const val RespKeyAuthUrl = "_wxapi_sendauth_resp_url"
    const val RespKeyErrorCode = "_wxapi_baseresp_errcode"
    const val RespKeyErrorStr = "_wxapi_baseresp_errstr"
    const val RespKeyOpenId = "_wxapi_baseresp_openId"
    /*end : 微信返回的数据的key*/

}