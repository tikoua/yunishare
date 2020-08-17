package com.tikoua.share.platform

import android.app.Activity
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.tencent.mm.opensdk.constants.Build
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.*
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.tikoua.share.model.*
import com.tikoua.share.utils.Util
import com.tikoua.share.wechat.WXConst
import com.tikoua.share.wechat.WechatShareMeta
import com.tikoua.share.wechat.loadWechatMeta
import com.uneed.yuni.BuildConfig
import com.uneed.yuni.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

/**
 *   created by dcl
 *   on 2020/8/13 10:46 AM
 *   api.registerApp: 未安装app时返回false
 */
class WechatPlatform : Platform {
    private var meta: WechatShareMeta? = null
    private var api: IWXAPI? = null
    private val thumbSize = 150
    private var shareEc: Int? = null
    override fun init(context: Context) {
        val api = getApi(context)
        val meta = getMeta(context)
        val registerApp = api.registerApp(meta.appid)
        log("registerApp: $registerApp")
        //建议动态监听微信启动广播进行注册到微信
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                log(intent?.dataString)
                val registerAppAgain = api.registerApp(meta.appid)
                log("registerAppAgain: $registerAppAgain")
            }
        }, IntentFilter(ConstantsAPI.ACTION_REFRESH_WXAPP))
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                log(intent?.dataString)
                intent?.let {
                    val errCode = it.getIntExtra("ec", 0)
                    log("errCode: $errCode")
                    shareEc = errCode
                }

            }
        }, IntentFilter(WXConst.ActionWXResp))
    }

    override fun support(type: ShareChannel): Boolean {
        return type == ShareChannel.WechatFriend || type == ShareChannel.WechatMoment
    }

    override suspend fun share(
        activity: Activity,
        shareChannel: ShareChannel,
        shareParams: InnerShareParams
    ): ShareResult {
        val wxAppSupportAPI = getApi(activity).wxAppSupportAPI
        if (wxAppSupportAPI == 0) {
            return ShareResult(ShareEc.NotInstall)
        }
        val checkVersion = checkVersion(activity, shareChannel)
        if (!checkVersion) {
            return ShareResult(ShareEc.PlatformUnSupport)
        }
        val type = shareParams.type
        if (ShareType.Video.type == type) {
            shareVideo(activity, shareParams, shareChannel)
            return ShareResult(ShareEc.Success)
        }
        val req: SendMessageToWX.Req =
            when (type) {
                ShareType.Text.type -> {
                    getShareTextReq(activity, shareParams).apply {
                        if (shareChannel == ShareChannel.WechatFriend) {
                            this.scene = SendMessageToWX.Req.WXSceneSession
                        } else if (shareChannel == ShareChannel.WechatMoment) {
                            this.scene = SendMessageToWX.Req.WXSceneTimeline
                        }
                    }
                }
                ShareType.Image.type -> {
                    getShareImageReq(activity, shareParams)?.apply {
                        if (shareChannel == ShareChannel.WechatFriend) {
                            this.scene = SendMessageToWX.Req.WXSceneSession
                        } else if (shareChannel == ShareChannel.WechatMoment) {
                            this.scene = SendMessageToWX.Req.WXSceneTimeline
                        }
                    }
                }
                ShareType.Video.type -> {
                    getShareVideoReq(activity, shareParams)?.apply {
                        if (shareChannel == ShareChannel.WechatFriend) {
                            this.scene = SendMessageToWX.Req.WXSceneSession
                        } else if (shareChannel == ShareChannel.WechatMoment) {
                            this.scene = SendMessageToWX.Req.WXSceneTimeline
                        }
                    }
                }
                ShareType.WechatMiniProgram.type -> {
                    if (shareChannel == ShareChannel.WechatFriend) {
                        getShareMiniProgramReq(activity, shareParams)?.apply {
                            this.scene = SendMessageToWX.Req.WXSceneSession
                        }
                    } else {
                        null
                    }
                }
                else -> null
            }
                ?: return ShareResult(ShareEc.PlatformUnSupport)
        shareEc = null
        val hashCode = activity.hashCode()
        activity.application.registerActivityLifecycleCallbacks(object :
            ActivityLifecycleCallback() {
            override fun onActivityResumed(activity: Activity) {
                if (hashCode == activity.hashCode()) {
                    activity.application.unregisterActivityLifecycleCallbacks(this)
                    GlobalScope.launch {
                        delay(1000)
                        if (shareEc == null) {
                            shareEc = BaseResp.ErrCode.ERR_USER_CANCEL
                        }
                    }

                }
            }
        })
        val sendReq = getApi(activity).sendReq(req)
        if (!sendReq) {
            return ShareResult(ShareEc.NotInstall)
        }
        var resultEc = 0
        while (true) {
            val result = shareEc
            if (result != null) {
                resultEc = result
                break
            }
            Log.v("WechatPlatform", "wait ...")
            delay(1000)
        }
        val ec = when (resultEc) {
            BaseResp.ErrCode.ERR_OK -> {
                ShareEc.Success
            }
            BaseResp.ErrCode.ERR_USER_CANCEL -> {
                ShareEc.Cancel
            }
            BaseResp.ErrCode.ERR_AUTH_DENIED -> {
                ShareEc.AuthDenied
            }
            BaseResp.ErrCode.ERR_UNSUPPORT -> {
                ShareEc.PlatformUnSupport
            }
            else -> {
                ShareEc.Unknown
            }
        }
        return ShareResult(ec)
    }


    /**
     * 生成文本分享的req
     */
    private fun getShareTextReq(
        context: Context,
        shareParams: InnerShareParams
    ): SendMessageToWX.Req {
        val textObj = WXTextObject()
        val text = shareParams.text
        textObj.text = text
        val msg = WXMediaMessage()
        msg.mediaObject = textObj
        msg.description = text
        val req = SendMessageToWX.Req()
        req.transaction = buildTransaction()
        req.message = msg
        return req
    }

    /**
     * 生成图片分享的req
     */
    private fun getShareImageReq(
        context: Context,
        shareParams: InnerShareParams
    ): SendMessageToWX.Req? {
        val path = shareParams.imagePath
        if (path.isNullOrEmpty()) {
            throw Exception("path can not be null")
        }
        val file = File(path)
        if (!file.exists()) {
            val tip: String =
                context.getString(R.string.send_img_file_not_exist)
            Toast.makeText(context, "$tip path = $path", Toast.LENGTH_LONG).show()
            return null
        }

        val imgObj = WXImageObject()
        imgObj.setImagePath(path)

        val msg = WXMediaMessage()
        msg.mediaObject = imgObj

        val bmp = BitmapFactory.decodeFile(path)
        val thumbBmp = Bitmap.createScaledBitmap(
            bmp,
            thumbSize,
            thumbSize,
            true
        )
        bmp.recycle()
        msg.thumbData = Util.bmpToByteArray(thumbBmp, true)
        val req = SendMessageToWX.Req()
        req.transaction = buildTransaction()
        req.message = msg
        return req
    }

    /**
     * 生成视频分享的req
     */
    private fun getShareVideoReq(
        context: Context,
        shareParams: InnerShareParams
    ): SendMessageToWX.Req? {
        val videoUrl = shareParams.videoUrl
        if (videoUrl.isNullOrEmpty()) {
            throw Exception("path can not be null")
        }
        val imgObj = WXVideoObject()
        imgObj.videoUrl = videoUrl
        val msg = WXMediaMessage()
        msg.mediaObject = imgObj
        msg.description = shareParams.desc
        msg.title = shareParams.title
        val req = SendMessageToWX.Req()
        req.transaction = buildTransaction()
        req.message = msg
        return req
    }

    /**
     * 生成小程序分享的req
     */
    private fun getShareMiniProgramReq(
        activity: Activity,
        shareParams: InnerShareParams
    ): SendMessageToWX.Req? {
        val pageUrl = shareParams.miniProgramWebPageUrl
        val miniProgramPath = shareParams.miniProgramPath
        val userName = shareParams.miniProgramUserName
        if (pageUrl.isNullOrEmpty() || miniProgramPath.isNullOrEmpty() || userName.isNullOrEmpty()) {
            throw Exception("pageUrl can not be null")
        }
        val wxMiniProgramObject = WXMiniProgramObject()
        wxMiniProgramObject.webpageUrl = pageUrl
        wxMiniProgramObject.miniprogramType = WXMiniProgramObject.MINIPTOGRAM_TYPE_RELEASE
        wxMiniProgramObject.path = miniProgramPath
        wxMiniProgramObject.userName = userName
        val msg = WXMediaMessage()
        msg.mediaObject = wxMiniProgramObject
        msg.description = shareParams.desc
        msg.title = shareParams.title
        msg.thumbData = shareParams.thumbData
        log("shareParams.thumbData: " + msg.thumbData?.size)
        val req = SendMessageToWX.Req()
        req.transaction = buildTransaction()
        req.message = msg
        return req
    }

    private fun shareVideo(
        activity: Activity,
        shareParams: InnerShareParams,
        shareChannel: ShareChannel
    ) {
        val videoUrl = shareParams.videoUrl
        val file = File(videoUrl)
        val intent = Intent("android.intent.action.SEND")
        val type = "video/*"
        val uri: Uri
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            uri = FileProvider.getUriForFile(activity, "com.uneed.yuni.fileProvider", file)
        } else {
            uri = Uri.fromFile(file)
        }
        intent.setDataAndType(uri, type)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val pkg = "com.tencent.mm"
        val cls =
            "com.tencent.mm.ui.tools.${if (shareChannel == ShareChannel.WechatMoment) "AddFavoriteUI" else "ShareImgUI"}"

        intent.component = ComponentName(pkg, cls)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        activity.startActivity(intent)
    }


    /**
     * 创建唯一的事务标志
     */
    private fun buildTransaction(): String {
        return UUID.randomUUID().toString()
    }

    private fun getApi(context: Context): IWXAPI {
        var api = api
        if (api != null) {
            return api
        }
        val meta = getMeta(context)
        api = WXAPIFactory.createWXAPI(context, meta.appid, true)
        this.api = api
        return api
    }

    private fun getMeta(context: Context): WechatShareMeta {
        var meta = meta
        if (meta != null) {
            return meta
        }
        meta = context.loadWechatMeta()
        this.meta = meta
        return meta
    }

    private fun log(msg: String?) {
        if (BuildConfig.DEBUG) {
            Log.d(javaClass.simpleName, "$msg")
        }
    }

    /**
     * 检查第三方的应用版本是否支持对应的分享方式
     */
    private fun checkVersion(context: Context, type: ShareChannel): Boolean {
        if (type == ShareChannel.WechatMoment) {
            return getApi(context).wxAppSupportAPI >= Build.TIMELINE_SUPPORTED_SDK_INT
        }
        return true
    }
}