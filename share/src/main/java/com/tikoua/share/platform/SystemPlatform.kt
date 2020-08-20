package com.tikoua.share.platform

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.tikoua.share.model.*
import com.tikoua.share.utils.checkEmpty
import java.io.File

/**
 *   created by dcl
 *   on 2020/8/14 6:15 PM
 */
class SystemPlatform : Platform {
    override fun init(context: Context) {

    }

    override fun support(type: ShareChannel): Boolean {
        return type == ShareChannel.System
    }

    override suspend fun share(
        activity: Activity,
        shareChannel: ShareChannel,
        shareParams: InnerShareParams
    ): ShareResult {
        val type = shareParams.type
        val intent = Intent(Intent.ACTION_SEND)
        when (type) {
            ShareType.Text.type -> {
                val text = shareParams.text
                val checkPath = text.checkEmpty("text")
                if (!checkPath.isNullOrEmpty()) {
                    return ShareResult(ShareEc.ParameterError)
                }
                intent.putExtra(Intent.EXTRA_TEXT, text)
                intent.type = "text/plain"
            }
            ShareType.Image.type -> {
                val imagePath = shareParams.imagePath
                val checkPath = imagePath.checkEmpty("imagePath")
                if (!checkPath.isNullOrEmpty()) {
                    return ShareResult(ShareEc.ParameterError)
                }
                makeMedia(activity, intent, imagePath!!, "image/*")
            }
            ShareType.Video.type -> {
                val imagePath = shareParams.videoPath
                val checkPath = imagePath.checkEmpty("videoPath")
                if (!checkPath.isNullOrEmpty()) {
                    return ShareResult(ShareEc.ParameterError)
                }
                makeMedia(activity, intent, imagePath!!, "video/*")
            }
            else -> {
                return ShareResult(ShareEc.PlatformUnSupport)
            }
        }
        activity.startActivity(intent)
        return ShareResult(ShareEc.Success)
    }

    override suspend fun auth(activity: Activity, channel: ShareChannel): AuthResult {
        TODO("Not yet implemented")
    }

    private fun makeMedia(
        context: Context,
        intent: Intent,
        filePath: String,
        contentType: String
    ): Intent {
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