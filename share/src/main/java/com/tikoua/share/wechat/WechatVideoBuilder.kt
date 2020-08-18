package com.tikoua.share.wechat

import com.tikoua.share.model.InnerShareParams
import com.tikoua.share.model.ShareType


/**
 *   created by dcl
 *   on 2020/8/11 6:21 PM
 *   只支持分享本地视频
 */
class WechatVideoBuilder() {
    /**
     * 视频文件的本地路径
     */
    private var videoPath: String? = null
    fun videoPath(videoPath: String?): WechatVideoBuilder {
        this.videoPath = videoPath
        return this
    }


    fun build(): InnerShareParams {
        return InnerShareParams().apply {
            this.videoPath = this@WechatVideoBuilder.videoPath
            type = ShareType.Video.type
        }
    }
}