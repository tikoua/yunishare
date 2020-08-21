package com.tikoua.share.qq

import com.tikoua.share.model.ShareParams
import com.tikoua.share.model.ShareType

/**
 *   created by dcl
 *   on 2020/8/17 4:31 PM
 */
class QQTextBuilder {
    private var text: String? = null
    private var desc: String? = null
    private var targetUrl: String? = null
    private var appName: String? = null
    fun text(text: String): QQTextBuilder {
        this.text = text
        return this
    }

    fun targetUrl(targetUrl: String): QQTextBuilder {
        this.targetUrl = targetUrl
        return this
    }

    fun appName(appName: String): QQTextBuilder {
        this.appName = appName
        return this
    }

    fun build(): ShareParams {
        if (text.isNullOrEmpty()) {
            throw Exception("title can not be null")
        }
        if (targetUrl.isNullOrEmpty()) {
            throw Exception("targetUrl can not be null")
        }
        return ShareParams().apply {
            this.type = ShareType.Text.type
            this.text = this@QQTextBuilder.text
            this.desc = this@QQTextBuilder.desc
            this.appName = this@QQTextBuilder.appName
            this.targetUrl = this@QQTextBuilder.targetUrl
        }
    }


}