package com.tikoua.share.wechat

import com.tikoua.share.model.InnerShareParams
import com.tikoua.share.model.ShareType


/**
 *   created by dcl
 *   on 2020/8/11 6:21 PM
 */
class WechatVideoBuilder() {
    private var videoUrl: String? = null
    private var title: String? = null
    private var desc: String? = null
    fun videoUrl(videoUrl: String?): WechatVideoBuilder {
        this.videoUrl = videoUrl
        return this
    }

    fun title(title: String?): WechatVideoBuilder {
        this.title = title
        return this
    }

    fun desc(desc: String?): WechatVideoBuilder {
        this.desc = desc
        return this
    }

    fun build(): InnerShareParams {
        return InnerShareParams().apply {
            this.videoUrl = this@WechatVideoBuilder.videoUrl
            this.title = this@WechatVideoBuilder.title
            this.desc = this@WechatVideoBuilder.desc
            type = ShareType.Video.type
        }
    }
}