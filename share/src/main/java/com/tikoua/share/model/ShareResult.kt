package com.tikoua.share.model

/**
 *   created by dcl
 *   on 2020/8/14 11:23 AM
 */
data class ShareResult(
    /**
     * [ShareEc]
     */
    val ec: Int,
    val em: String? = null      //捕获到某些异常时提供更具体的异常信息
)