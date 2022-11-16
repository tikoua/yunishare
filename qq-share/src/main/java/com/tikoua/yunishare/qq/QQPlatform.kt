package com.tikoua.yunishare.qq

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.content.FileProvider
import com.tencent.connect.share.QQShare
import com.tencent.connect.share.QQShare.SHARE_TO_QQ_TYPE_DEFAULT
import com.tencent.connect.share.QzonePublish
import com.tencent.connect.share.QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import com.tikoua.share.model.*
import com.tikoua.share.platform.Platform
import com.tikoua.share.utils.checkEmpty
import com.tikoua.share.utils.log
import com.tikoua.share.utils.toChooser
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
    private var isPrepare = false

    /**
     * 在用户同意隐私协议之后调用
     */
    override fun init(context: Context) {
        super.init(context)
        val prepare = isPrepare(context)
        if (!prepare) {
            return
        }
        Tencent.setIsPermissionGranted(true,Build.MODEL)
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
        shareParams: ShareParams
    ): ShareResult {
        if (!isPrepare) {
            return ShareResult(ShareEc.CannotFindMeta)
        }
        return tencentClient?.let {
            val qqInstalled = it.isQQInstalled(activity)
            if (!qqInstalled) {
                return@let ShareResult(ShareEc.NotInstall)
            }
            return@let when (shareParams.type) {
                ShareType.Text.type -> {
                    val text = shareParams.text ?: ""
                    sharePlainText(activity, shareChannel, text)
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

    override suspend fun auth(activity: Activity, channel: ShareChannel): AuthResult {
        if (!isPrepare) {
            return AuthResult(ShareEc.CannotFindMeta)
        }
        TODO("Not yet implemented")
    }

    private fun isPrepare(context: Context): Boolean {
        if (isPrepare) {
            return true
        }
        return (try {
            getMeta(context)
        } catch (error: Throwable) {
            null
        } != null).apply {
            isPrepare = this
        }
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
    private suspend fun sharePlainText(
        activity: Activity,
        channel: ShareChannel,
        text: String
    ): ShareResult {
        if (channel == ShareChannel.QQFriend) {
            val intent = makeIntent()
            intent.putExtra(Intent.EXTRA_TEXT, text)
            intent.type = "text/plain"
            activity.startActivity(intent.toChooser(activity))
            return ShareResult(ShareEc.Success)
        } else {
            val params = Bundle().apply {
                putInt(QzonePublish.PUBLISH_TO_QZONE_KEY_TYPE, PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD)
                putString(QzonePublish.PUBLISH_TO_QZONE_SUMMARY, text)
            }
            return doShare(activity, channel, params)
        }
    }

    /**
     * 分享本地图片
     */
    private suspend fun shareImage(
        activity: Activity,
        channel: ShareChannel,
        shareParams: ShareParams
    ): ShareResult {
        val imagePath = shareParams.imagePath
        val checkEmpty = imagePath.checkEmpty("imagePath")
        if (checkEmpty != null) {
            return ShareResult(ShareEc.ParameterError, checkEmpty)
        }
        return if (channel == ShareChannel.QQZone) {
            val params = Bundle().apply {
                putInt(QzonePublish.PUBLISH_TO_QZONE_KEY_TYPE, PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD)
                putStringArrayList(
                    QzonePublish.PUBLISH_TO_QZONE_IMAGE_URL,
                    ArrayList<String?>().apply {
                        add(imagePath)
                    })
            }
            doShare(activity, channel, params)
        } else {
            val intent = makeMediaIntent(activity, imagePath!!, "image/*")
            activity.startActivity(intent.toChooser(activity))
            ShareResult(ShareEc.Success)
        }

    }

    /**
     * 分享本地视频
     */
    private suspend fun shareVideo(
        activity: Activity,
        channel: ShareChannel,
        shareParams: ShareParams
    ): ShareResult {
        val imagePath = shareParams.videoPath
        val checkEmpty = imagePath.checkEmpty("videoPath")
        if (checkEmpty != null) {
            return ShareResult(ShareEc.ParameterError, checkEmpty)
        }
        return if (channel == ShareChannel.QQZone) {
            val params = Bundle().apply {
                putInt(
                    QzonePublish.PUBLISH_TO_QZONE_KEY_TYPE,
                    QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHVIDEO
                )
                putString(QzonePublish.PUBLISH_TO_QZONE_VIDEO_PATH, imagePath)
            }
            doShare(activity, channel, params)
        } else {
            val intent = makeMediaIntent(activity, imagePath!!, "video/*")
            activity.startActivity(intent.toChooser(activity))
            ShareResult(ShareEc.Success)
        }
    }

    /**
     * 分享链接
     * 使用sdk
     */
    private suspend fun shareLink(
        activity: Activity,
        channel: ShareChannel,
        shareParams: ShareParams
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
        return doShare(activity, channel, params, true)
    }

    /**
     *  @param isLink 分享链接到空间需要使用shareToQzone 其他的使用publishToQzone
     */
    private suspend fun doShare(
        activity: Activity,
        channel: ShareChannel,
        params: Bundle,
        isLink: Boolean = false
    ): ShareResult {
        var ec: Int? = null
        val shareListener = object : IUiListener {
            override fun onComplete(p0: Any?) {
                ec = ShareEc.Success
            }

            override fun onCancel() {
                ec = ShareEc.Cancel
            }

            override fun onWarning(p0: Int) {
                log("onWarning: ${p0}", javaClass.simpleName)
            }

            override fun onError(p0: UiError?) {
                log("onError: ${p0}", javaClass.simpleName)
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
                            ec = ShareEc.Success
                        }
                    }
                }
            }
        })
        if (channel == ShareChannel.QQZone) {
            if (isLink) {
                tencentClient?.shareToQzone(activity, params, shareListener)
            } else {
                tencentClient?.publishToQzone(activity, params, shareListener)
            }
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