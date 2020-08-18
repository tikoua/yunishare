package com.tikoua.share.model

/**
 *   created by dcl
 *   on 2020/8/14 11:27 AM
 */
object ShareEc {
    const val Unknown = 0           //成功
    const val Success = 1           //成功
    const val Cancel = 2           //用户取消
    const val Unsupported = 3           //与你不支持的分享渠道
    const val PlatformUnSupport = 4     //第三方渠道暂不支持这种分享
    const val NotInstall = 5     //未安装指定的第三方应用
    const val AuthDenied = 6     //授权失败
    const val ParameterError = 7     //传递的参数不合规

}