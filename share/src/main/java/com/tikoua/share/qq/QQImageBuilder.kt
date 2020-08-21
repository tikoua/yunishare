package com.tikoua.share.qq

import com.tikoua.share.model.ShareParams
import com.tikoua.share.model.ShareType

/**
 *   created by dcl
 *   on 2020/8/17 4:31 PM
 */
class QQImageBuilder {
    private var imagePath: String? = null
    private var imageUrl: String? = null
    private var appName: String? = null
    private var title: String? = null
    private var desc: String? = null
    fun imagePath(imagePath: String): QQImageBuilder {
        this.imagePath = imagePath
        return this
    }

    fun imageUrl(imageUrl: String): QQImageBuilder {
        this.imageUrl = imageUrl
        return this
    }

    fun title(title: String): QQImageBuilder {
        this.title = title
        return this
    }

    fun desc(desc: String): QQImageBuilder {
        this.desc = desc
        return this
    }

    fun appName(appName: String): QQImageBuilder {
        this.appName = appName
        return this
    }

    fun build(): ShareParams {
        return ShareParams().apply {
            this.type = ShareType.Image.type
            val path = this@QQImageBuilder.imagePath
            val url = this@QQImageBuilder.imageUrl
            val title = this@QQImageBuilder.title
            if (path.isNullOrEmpty() && url.isNullOrEmpty()) {
                throw Exception("image is null")
            }
            if (path.isNullOrEmpty() && title.isNullOrEmpty()) {
                throw Exception("分享远程链接时需要设置title")
            }

            this.imagePath = path
            this.imageUrl = url
            this.appName = this@QQImageBuilder.appName
            this.title = this@QQImageBuilder.title
            this.desc = this@QQImageBuilder.desc
            this.targetUrl = url
        }
    }
}