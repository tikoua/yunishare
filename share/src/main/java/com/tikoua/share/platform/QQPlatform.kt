package com.tikoua.share.platform

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.tencent.connect.share.QQShare
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import com.tikoua.share.model.*
import com.tikoua.share.qq.QQShareMeta
import com.tikoua.share.qq.loadQQMeta
import com.tikoua.share.utils.log


/**
 *   created by dcl
 *   on 2020/8/14 6:15 PM
 */
class QQPlatform : Platform {
    private var meta: QQShareMeta? = null
    private var tencentClient: Tencent? = null
    override fun init(context: Context) {
        tencentClient =
            Tencent.createInstance(
                getMeta(context).appid,
                context/*, "com.uneed.yuni.fileProvider"*/
            )
    }

    override fun support(type: ShareChannel): Boolean {
        return type == ShareChannel.QQZone || type == ShareChannel.QQFriend
    }

    override suspend fun share(
        activity: Activity,
        shareChannel: ShareChannel,
        shareParams: InnerShareParams
    ): ShareResult {
        return tencentClient?.let {
            val qqInstalled = it.isQQInstalled(activity)
            if (!qqInstalled) {
                return@let ShareResult(ShareEc.NotInstall)
            }
            val type = shareParams.type
            if (!(type == ShareType.Text.type || type == ShareType.Image.type || type == ShareType.Video.type)) {
                return@let ShareResult(ShareEc.PlatformUnSupport)
            }
            if (type == ShareType.Text.type) {
                val text = shareParams.text!!
                sharePlainText(activity, text)
                return@let ShareResult(ShareEc.Success)
            }
            var ec: Int = ShareEc.Success
            var qqType: Int = QQShare.SHARE_TO_QQ_TYPE_DEFAULT
            when (type) {
                ShareType.Image.type -> {
                    val title = shareParams.title
                    qqType =
                        if (title.isNullOrEmpty()) QQShare.SHARE_TO_QQ_TYPE_IMAGE else QQShare.SHARE_TO_QQ_TYPE_DEFAULT
                }
                ShareType.Video.type -> {
                    qqType = QQShare.SHARE_TO_QQ_TYPE_DEFAULT
                }
                else -> ec = ShareEc.PlatformUnSupport
            }
            if (ec != ShareEc.Success) {
                return@let ShareResult(ec)
            }
            val params1 = Bundle().apply {
                putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, qqType)
                putString(QQShare.SHARE_TO_QQ_TITLE, shareParams.title)
                putString(QQShare.SHARE_TO_QQ_SUMMARY, shareParams.desc)
                putString(QQShare.SHARE_TO_QQ_TARGET_URL, shareParams.targetUrl)
                putString(QQShare.SHARE_TO_QQ_APP_NAME, shareParams.appName)
                putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, shareParams.imagePath)
                putString(QQShare.SHARE_TO_QQ_IMAGE_URL, shareParams.imageUrl)
            }
            it.shareToQQ(activity, params1, object : IUiListener {
                override fun onComplete(p0: Any?) {
                    log("onComplete: $p0")
                }

                override fun onCancel() {
                    log("onCancel ")
                }

                override fun onError(p0: UiError?) {
                    log("onError: $p0")
                }

            })
            ShareResult(ShareEc.PlatformUnSupport)
        } ?: ShareResult(ShareEc.PlatformUnSupport)
    }

    private fun getMeta(context: Context): QQShareMeta {
        var meta = meta
        if (meta != null) {
            return meta
        }
        meta = context.loadQQMeta()
        this.meta = meta
        return meta
    }

    /**
     * qq分享的sdk本身不支持纯文本分享
     */
    private fun sharePlainText(activity: Activity, text: String) {
        val intent = Intent("android.intent.action.SEND")
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, "与你")
        intent.putExtra(Intent.EXTRA_TEXT, text)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.component = ComponentName(
            "com.tencent.mobileqq",
            "com.tencent.mobileqq.activity.JumpActivity"
        )
        activity.startActivity(intent)
    }
}