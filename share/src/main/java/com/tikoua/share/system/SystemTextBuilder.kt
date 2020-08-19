package com.tikoua.share.system

import com.tikoua.share.model.InnerShareParams
import com.tikoua.share.model.ShareType

/**
 *   created by dcl
 *   on 2020/8/19 4:00 PM
 */
class SystemTextBuilder {
    private var text: String? = null
    fun text(text: String): SystemTextBuilder {
        this.text = text
        return this
    }

    fun build(): InnerShareParams {
        return InnerShareParams().apply {
            type = ShareType.Text.type
            text = this@SystemTextBuilder.text
        }
    }
}