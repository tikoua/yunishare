package com.tikoua.yunishare.qq

import com.tikoua.share.model.ShareParams
import com.tikoua.share.model.ShareType

/**
 *   created by dcl
 *   on 2020/8/19 10:58 AM
 */
class QQLinkBuilder {
    /**
     * 最长30个字符
     */
    private var title: String? = null

    /**
     * 最长40个字符
     */
    private var desc: String? = null
    private var link: String? = null

    /**
     * 图片的本地或远程链接
     */
    private var cover: String? = null
    fun title(title: String?): QQLinkBuilder {
        this.title = title
        return this
    }

    fun desc(desc: String?): QQLinkBuilder {
        this.desc = desc
        return this
    }

    fun link(link: String?): QQLinkBuilder {
        this.link = link
        return this
    }

    fun cover(cover: String?): QQLinkBuilder {
        this.cover = cover
        return this
    }


    fun build(): ShareParams {
        return ShareParams().apply {
            this.type = ShareType.Link.type
            this.title = this@QQLinkBuilder.title
            this.desc = this@QQLinkBuilder.desc
            this.link = this@QQLinkBuilder.link
            this.imageUrl = this@QQLinkBuilder.cover
        }
    }
}