package com.uneed.yuni.wxapi

import com.tikoua.share.utils.log
import com.tikoua.share.wechat.WechatHandlerActivity

class WXEntryActivity : WechatHandlerActivity() {
    /**
     * 处理微信发出的向第三方应用请求app message
     *
     *
     * 在微信客户端中的聊天页面有“添加工具”，可以将本应用的图标添加到其中
     * 此后点击图标，下面的代码会被执行。Demo仅仅只是打开自己而已，但你可
     * 做点其他的事情，包括根本不打开任何页面
     */
    override fun onGetMessageFromWXReq(msg: Any?) {
        log("onGetMessageFromWXReq: $msg")
        if (msg != null) {
            val iLaunchMyself =
                packageManager.getLaunchIntentForPackage(packageName)
            startActivity(iLaunchMyself)
        }
    }

    /**
     * 处理微信向第三方应用发起的消息
     *
     *
     * 此处用来接收从微信发送过来的消息，比方说本demo在wechatpage里面分享
     * 应用时可以不分享应用文件，而分享一段应用的自定义信息。接受方的微信
     * 客户端会通过这个方法，将这个信息发送回接收方手机上的本demo中，当作
     * 回调。
     *
     *
     * 本Demo只是将信息展示出来，但你可做点其他的事情，而不仅仅只是Toast
     */
    override fun onShowMessageFromWXReq(msg: Any?) {
        log("onShowMessageFromWXReq: $msg")
        if (msg != null) {
            val iLaunchMyself =
                packageManager.getLaunchIntentForPackage(packageName)
            startActivity(iLaunchMyself)
        }
    }
}