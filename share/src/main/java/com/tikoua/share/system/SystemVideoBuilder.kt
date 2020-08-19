package com.tikoua.share.system

import com.tikoua.share.model.InnerShareParams
import com.tikoua.share.model.ShareType

/**
 *   created by dcl
 *   on 2020/8/19 4:00 PM
 */
class SystemVideoBuilder {
    private var videoPath: String? = null
    fun videoPath(videoPath: String): SystemVideoBuilder {
        this.videoPath = videoPath
        return this
    }

    fun build(): InnerShareParams {
        return InnerShareParams().apply {
            type = ShareType.Video.type
            videoPath = this@SystemVideoBuilder.videoPath
        }
    }
}