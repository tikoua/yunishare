package com.tikoua.share.wechat

import com.tikoua.share.model.InnerShareParams
import com.tikoua.share.model.ShareType


/**
 *   created by dcl
 *   on 2020/8/11 6:21 PM
 */
class WechatTextBuilder() {
    private var text: String? = null
    fun text(text: String?): WechatTextBuilder {
        this.text = text
        return this
    }

    fun build(): InnerShareParams {
        val text = text
        if (text.isNullOrEmpty()) {
            throw Exception("text can not be null")
        }
        return InnerShareParams().apply {
            this.type = ShareType.Text.type
            this.text = text
        }
    }
}