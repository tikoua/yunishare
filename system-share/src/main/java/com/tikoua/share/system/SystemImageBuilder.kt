package com.tikoua.share.system

import com.tikoua.share.model.ShareParams
import com.tikoua.share.model.ShareType

/**
 *   created by dcl
 *   on 2020/8/19 4:00 PM
 */
class SystemImageBuilder {
    private var imagePath: String? = null
    fun imagePath(imagePath: String): SystemImageBuilder {
        this.imagePath = imagePath
        return this
    }

    fun build(): ShareParams {
        return ShareParams().apply {
            type = ShareType.Image.type
            imagePath = this@SystemImageBuilder.imagePath
        }
    }
}