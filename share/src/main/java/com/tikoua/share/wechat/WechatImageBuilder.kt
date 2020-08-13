package com.tikoua.share.wechat

import com.tikoua.share.model.InnerShareParams
import com.tikoua.share.model.ShareType


/**
 *   created by dcl
 *   on 2020/8/11 6:21 PM
 */
class WechatImageBuilder() {
    private var imagePath: String? = null
    private var title: String? = null
    fun imagePath(imagePath: String?): WechatImageBuilder {
        this.imagePath = imagePath
        return this
    }

    fun title(title: String?): WechatImageBuilder {
        this.title = title
        return this
    }

    fun build(): InnerShareParams {
        val path = imagePath
        if (path.isNullOrEmpty()) {
            throw Exception("imagePath can not be null")
        }
        return InnerShareParams().apply {
            this.imagePath = path
            this.title = this@WechatImageBuilder.title
            type = ShareType.Image.type
        }
    }
}