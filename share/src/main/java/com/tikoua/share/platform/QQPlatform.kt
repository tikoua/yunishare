package com.tikoua.share.platform

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.tencent.tauth.Tencent
import com.tikoua.share.model.*
import com.tikoua.share.qq.QQShareMeta
import com.tikoua.share.qq.loadQQMeta
import com.tikoua.share.utils.checkEmpty
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
                    sharePlainText(activity, text + System.currentTimeMillis())
                }
                ShareType.Image.type -> {
                    shareImage(activity, shareParams)
                }
                ShareType.Video.type -> {
                    shareVideo(activity, shareParams)
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
    private fun sharePlainText(activity: Activity, text: String): ShareResult {
        val intent = makeIntent()
        intent.putExtra(Intent.EXTRA_SUBJECT, "与你")
        intent.putExtra(Intent.EXTRA_TEXT, text)
        intent.type = "text/plain"
        activity.startActivity(intent)
        return ShareResult(ShareEc.Success)
    }

    /**
     * 分享本地图片
     */
    private fun shareImage(activity: Activity, shareParams: InnerShareParams): ShareResult {
        val imagePath = shareParams.imagePath
        val checkEmpty = imagePath.checkEmpty("imagePath")
        if (checkEmpty != null) {
            return ShareResult(ShareEc.ParameterError, checkEmpty)
        }
        val intent = makeMediaIntent(activity, imagePath!!, "image/*")
        activity.startActivity(intent)
        return ShareResult(ShareEc.Success)
    }

    private fun shareVideo(activity: Activity, shareParams: InnerShareParams): ShareResult {
        val imagePath = shareParams.videoPath
        val checkEmpty = imagePath.checkEmpty("videoPath")
        if (checkEmpty != null) {
            return ShareResult(ShareEc.ParameterError, checkEmpty)
        }
        val intent = makeMediaIntent(activity, imagePath!!, "video/*")
        activity.startActivity(intent)
        return ShareResult(ShareEc.Success)
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