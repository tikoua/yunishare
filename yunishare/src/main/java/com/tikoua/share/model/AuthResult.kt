package com.tikoua.share.model

/**
 *   created by dcl
 *   on 2020/8/14 11:23 AM
 */
data class AuthResult(
    /**
     * [ShareEc]
     */
    val ec: Int,
    //捕获到某些异常时提供更具体的异常信息
    val em: String? = null,
    /**
     * 第三方平台授权后湖区到的用户信息
     * 各个平台有不同的结构
     */
    val authData: Any? = null
) {
    fun isSuccess(): Boolean {
        return ec == ShareEc.Success
    }
}