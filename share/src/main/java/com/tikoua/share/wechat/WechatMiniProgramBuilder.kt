package com.tikoua.share.wechat

import com.tikoua.share.model.InnerShareParams
import com.tikoua.share.model.ShareType


/**
 *   created by dcl
 *   on 2020/8/11 6:21 PM
 */
class WechatMiniProgramBuilder() {
    private var text: String? = null
    private var title: String? = null

    /**
     * 兼容低版本的网页链接
     * 限制长度不超过 10KB
     */
    private var webPageUrl: String? = null

    /**
     * 小程序的原始 id
     * 小程序原始 ID 获取方法：登录小程序管理后台-设置-基本设置-帐号信息
     */
    private var userName: String? = null

    /**
     * 小程序的 path
     * 小程序页面路径；对于小游戏，可以只传入 query 部分，来实现传参效果，如：传入 "?foo=bar"
     */
    private var path: String? = null

    /**
     * 是否使用带 shareTicket 的分享
     * 通常开发者希望分享出去的小程序被二次打开时可以获取到更多信息，例如群的标识。
     * 可以设置 withShareTicket 为 true，当分享卡片在群聊中被其他用户打开时，
     * 可以获取到 shareTicket，用于获取更多分享信息。详见小程序获取更多分享信息 ，最低客户端版本要求：6.5.13
     */
    private var withShareTicket: Boolean? = null

    /**
     * 小程序的类型，默认正式版
     *  正式版: WXMiniProgramObject.MINIPTOGRAM_TYPE_RELEASE;
    测试版: WXMiniProgramObject.MINIPROGRAM_TYPE_TEST;
    预览版: WXMiniProgramObject.MINIPROGRAM_TYPE_PREVIEW
     */
    private var miniProgramType: Int? = null

    fun webPageUrl(webPageUrl: String?): WechatMiniProgramBuilder {
        this.webPageUrl = webPageUrl
        return this
    }

    fun userName(userName: String?): WechatMiniProgramBuilder {
        this.userName = userName
        return this
    }

    fun path(path: String?): WechatMiniProgramBuilder {
        this.path = path
        return this
    }

    fun withShareTicket(withShareTicket: Boolean?): WechatMiniProgramBuilder {
        this.withShareTicket = withShareTicket
        return this
    }

    fun miniProgramType(miniProgramType: Int?): WechatMiniProgramBuilder {
        this.miniProgramType = miniProgramType
        return this
    }

    fun build(): InnerShareParams {
        val text = text
        if (text.isNullOrEmpty()) {
            throw Exception("text can not be null")
        }
        return InnerShareParams().apply {
            this.type = ShareType.WechatMiniProgram.type
            this.miniProgramWebPageUrl = this@WechatMiniProgramBuilder.webPageUrl
            this.miniProgramUserName = this@WechatMiniProgramBuilder.userName
            this.miniProgramPath = this@WechatMiniProgramBuilder.path
            this.miniProgramWithShareTicket = this@WechatMiniProgramBuilder.withShareTicket
            this.miniProgramType = this@WechatMiniProgramBuilder.miniProgramType
        }
    }
}