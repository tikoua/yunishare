package com.tikoua.share.wechat

import com.tikoua.share.model.ShareParams
import com.tikoua.share.model.ShareType


/**
 *   created by dcl
 *   on 2020/8/11 6:21 PM
 */
class WechatLinkBuilder() {
    private var title: String? = null
    private var desc: String? = null
    private var link: String? = null

    /**
     * 需要小于128kb
     */
    private var thumbData: ByteArray? = null
    fun title(title: String?): WechatLinkBuilder {
        this.title = title
        return this
    }

    fun desc(desc: String?): WechatLinkBuilder {
        this.desc = desc
        return this
    }

    fun link(link: String?): WechatLinkBuilder {
        this.link = link
        return this
    }

    fun thumbData(thumbData: ByteArray?): WechatLinkBuilder {
        this.thumbData = thumbData
        return this
    }

    fun build(): ShareParams {
        return ShareParams().apply {
            this.type = ShareType.Link.type
            this.title = this@WechatLinkBuilder.title
            this.desc = this@WechatLinkBuilder.desc
            this.link = this@WechatLinkBuilder.link
            this.thumbData = this@WechatLinkBuilder.thumbData

        }
    }
}