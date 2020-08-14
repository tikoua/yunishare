package com.tikoua.share.platform

import android.app.Activity
import android.content.Context
import com.tikoua.share.model.InnerShareParams
import com.tikoua.share.model.ShareChannel
import com.tikoua.share.model.ShareResult

/**
 *   created by dcl
 *   on 2020/8/14 6:15 PM
 */
class TwitterPlatform : Platform {
    override fun init(context: Context) {

    }

    override fun support(type: ShareChannel): Boolean {
        return false
    }

    override suspend fun share(
        activity: Activity,
        type: ShareChannel,
        shareParams: InnerShareParams
    ): ShareResult {
        TODO("Not yet implemented")
    }
}