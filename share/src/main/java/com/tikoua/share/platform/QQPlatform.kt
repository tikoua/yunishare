package com.tikoua.share.platform

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.content.FileProvider
import com.tencent.connect.share.QQShare
import com.tencent.connect.share.QQShare.SHARE_TO_QQ_TYPE_DEFAULT
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import com.tikoua.share.model.*
import com.tikoua.share.qq.QQShareMeta
import com.tikoua.share.qq.loadQQMeta
import com.tikoua.share.utils.checkEmpty
import com.tikoua.share.utils.log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File


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
                context,
                "${context.packageName}.fileProvider"
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
            return@let when (shareParams.type) {
                ShareType.Text.type -> {
                    val text = shareParams.text!!
                    sharePlainText(activity, shareChannel, text + System.currentTimeMillis())
                }
                ShareType.Image.type -> {
                    shareImage(activity, shareChannel, shareParams)
                }
                ShareType.Video.type -> {
                    shareVideo(activity, shareChannel, shareParams)
                }
                ShareType.Link.type -> {
                    shareLink(activity, shareChannel, shareParams)
                }
                else -> {
                    ShareResult(ShareEc.PlatformUnSupport)
                }
            }
        } ?: ShareResult(ShareEc.NotInstall)
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
    private fun sharePlainText(
        activity: Activity,
        channel: ShareChannel,
        text: String
    ): ShareResult {
        val intent = makeIntent()
        intent.putExtra(Intent.EXTRA_TEXT, text)
        intent.type = "text/plain"
        activity.startActivity(intent)
        return ShareResult(ShareEc.Success)
    }

    /**
     * 分享本地图片
     */
    private fun shareImage(
        activity: Activity,
        channel: ShareChannel,
        shareParams: InnerShareParams
    ): ShareResult {
        val imagePath = shareParams.imagePath
        val checkEmpty = imagePath.checkEmpty("imagePath")
        if (checkEmpty != null) {
            return ShareResult(ShareEc.ParameterError, checkEmpty)
        }
        val intent = makeMediaIntent(activity, imagePath!!, "image/*")
        activity.startActivity(intent)
        return ShareResult(ShareEc.Success)
    }

    /**
     * 分享本地视频
     */
    private fun shareVideo(
        activity: Activity,
        channel: ShareChannel,
        shareParams: InnerShareParams
    ): ShareResult {
        val imagePath = shareParams.videoPath
        val checkEmpty = imagePath.checkEmpty("videoPath")
        if (checkEmpty != null) {
            return ShareResult(ShareEc.ParameterError, checkEmpty)
        }
        val intent = makeMediaIntent(activity, imagePath!!, "video/*")
        activity.startActivity(intent)
        return ShareResult(ShareEc.Success)
    }

    /**
     * 分享链接
     * 使用sdk
     */
    private suspend fun shareLink(
        activity: Activity,
        channel: ShareChannel,
        shareParams: InnerShareParams
    ): ShareResult {
        val title = shareParams.title
        val desc = shareParams.desc
        val url = shareParams.link
        val cover = shareParams.imageUrl
        val checkUrl = url.checkEmpty("link")
        if (checkUrl != null) {
            return ShareResult(ShareEc.ParameterError, checkUrl)
        }
        val params = Bundle().apply {
            putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, SHARE_TO_QQ_TYPE_DEFAULT)
            putString(QQShare.SHARE_TO_QQ_TITLE, title)
            putString(QQShare.SHARE_TO_QQ_SUMMARY, desc)
            putString(QQShare.SHARE_TO_QQ_TARGET_URL, url)
            if (channel == ShareChannel.QQZone) {
                putStringArrayList(
                    QQShare.SHARE_TO_QQ_IMAGE_URL,
                    ArrayList<String?>().apply {
                        add(cover)
                    }
                )
            } else {
                putString(
                    QQShare.SHARE_TO_QQ_IMAGE_URL,
                    cover
                )
            }
        }
        return doShare(activity, channel, params)
    }

    private suspend fun doShare(
        activity: Activity,
        channel: ShareChannel,
        params: Bundle
    ): ShareResult {
        var ec: Int? = null
        val shareListener = object : IUiListener {
            override fun onComplete(p0: Any?) {
                ec = ShareEc.Success
            }

            override fun onCancel() {
                ec = ShareEc.Cancel
            }

            override fun onError(p0: UiError?) {
                ec = ShareEc.PlatformError
            }
        }
        val hashCode = activity.hashCode()
        activity.application.registerActivityLifecycleCallbacks(object :
            ActivityLifecycleCallback() {
            override fun onActivityResumed(activity: Activity) {
                if (hashCode == activity.hashCode()) {
                    activity.application.unregisterActivityLifecycleCallbacks(this)
                    GlobalScope.launch {
                        delay(500)
                        if (ec == null) {
                            //没返回错误码也当做成功
                            ec = BaseResp.ErrCode.ERR_OK
                        }
                    }
                }
            }
        })
        if (channel == ShareChannel.QQZone) {
            tencentClient?.shareToQzone(activity, params, shareListener)
        } else {
            tencentClient?.shareToQQ(activity, params, shareListener)
        }
        while (ec == null) {
            log("wait...")
            delay(500)
        }
        return ShareResult(ec!!)

    }

    private fun makeIntent(): Intent {
        val intent = Intent(Intent.ACTION_SEND)
        intent.component = ComponentName(
            "com.tencent.mobileqq",
            "com.tencent.mobileqq.activity.JumpActivity"
        )
        return intent
    }

    private fun makeMediaIntent(context: Context, filePath: String, contentType: String): Intent {
        val intent = makeIntent()
        val uri: Uri
        val file = File(filePath)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            uri = FileProvider.getUriForFile(context, "${context.packageName}.fileProvider", file)
        } else {
            uri = Uri.fromFile(file)
        }
        intent.type = contentType
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        return intent
    }
}