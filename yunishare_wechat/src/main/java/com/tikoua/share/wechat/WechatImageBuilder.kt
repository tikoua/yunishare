package com.tikoua.share.wechat

import com.tikoua.share.model.ShareParams
import com.tikoua.share.model.ShareType


/**
 *   created by dcl
 *   on 2020/8/11 6:21 PM
 *   只支持分享本地图片
 */
class WechatImageBuilder() {
    /**
     * 要分享的图片的本地路径
     */
    private var imagePath: String? = null
    fun imagePath(imagePath: String?): WechatImageBuilder {
        this.imagePath = imagePath
        return this
    }

    fun build(): ShareParams {
        val path = imagePath
        if (path.isNullOrEmpty()) {
            throw Exception("imagePath can not be null")
        }
        return ShareParams().apply {
            this.imagePath = path
            type = ShareType.Image.type
        }
    }
}