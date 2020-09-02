package com.tikoua.yunishare.qq

import com.tikoua.share.model.ShareParams
import com.tikoua.share.model.ShareType

/**
 *   created by dcl
 *   on 2020/8/17 4:31 PM
 */
class QQVideoBuilder {
    private var videoPath: String? = null
    fun videoPath(videoPath: String): QQVideoBuilder {
        this.videoPath = videoPath
        return this
    }

    fun build(): ShareParams {
        return ShareParams().apply {
            this.type = ShareType.Video.type
            this.videoPath = this@QQVideoBuilder.videoPath
        }
    }
}