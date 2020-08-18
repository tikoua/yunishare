package com.tikoua.share.platform

import android.app.Activity
import android.content.*
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.tencent.mm.opensdk.constants.Build
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage
import com.tencent.mm.opensdk.modelmsg.WXMiniProgramObject
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.tikoua.share.model.*
import com.tikoua.share.utils.checkEmpty
import com.tikoua.share.wechat.WXConst
import com.tikoua.share.wechat.WechatShareMeta
import com.tikoua.share.wechat.loadWechatMeta
import com.uneed.yuni.BuildConfig
import kotlinx.coroutines.*
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
        return when (type) {
            ShareType.Text.type -> {
                shareText(activity, shareParams, shareChannel)
            }
            ShareType.Image.type -> {
                shareImage(activity, shareParams, shareChannel)
            }
            ShareType.Video.type -> {
                shareVideo(activity, shareParams, shareChannel)
            }
            ShareType.WechatMiniProgram.type -> {
                shareMiniProgram(activity, shareParams, shareChannel)
            }
            else -> ShareResult(ShareEc.PlatformUnSupport)
        }.also {
            if (!it.isSuccess()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(activity, "分享失败: ${it.ec}  ${it.em} ", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * 生成小程序分享的req
     */
    private fun getShareMiniProgramReq(
        activity: Activity,
        shareParams: InnerShareParams
    ): SendMessageToWX.Req {
        val pageUrl = shareParams.miniProgramWebPageUrl
        val miniProgramPath = shareParams.miniProgramPath
        val userName = shareParams.miniProgramUserName
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

    /**
     * 文字使用系统分享
     */
    private fun shareText(
        activity: Activity,
        shareParams: InnerShareParams,
        shareChannel: ShareChannel
    ): ShareResult {
        //不支持分享纯文本到朋友圈
        if (shareChannel != ShareChannel.WechatFriend) {
            return ShareResult(ShareEc.PlatformUnSupport)
        }
        val intent = makeTextIntent(shareChannel, shareParams.text!!)
        activity.startActivity(intent)
        return ShareResult(ShareEc.Success)
    }

    /**
     * 图片使用系统分享
     */
    private fun shareImage(
        activity: Activity,
        shareParams: InnerShareParams,
        shareChannel: ShareChannel
    ): ShareResult {
        val imagePath = shareParams.imagePath!!
        val intent = makeWechatMediaIntent(activity, shareChannel, imagePath, "image/*")
        activity.startActivity(intent)
        return ShareResult(ShareEc.Success)
    }

    /**
     * 视频使用系统分享
     */
    private fun shareVideo(
        activity: Activity,
        shareParams: InnerShareParams,
        shareChannel: ShareChannel
    ): ShareResult {
        //不支持分享视频到朋友圈
        if (shareChannel != ShareChannel.WechatFriend) {
            return ShareResult(ShareEc.PlatformUnSupport)
        }
        val videoUrl = shareParams.videoPath!!
        val intent = makeWechatMediaIntent(activity, shareChannel, videoUrl, "video/*")
        activity.startActivity(intent)
        return ShareResult(ShareEc.Success)
    }

    /**
     *使用sdk分享小程序到会话
     */
    private suspend fun shareMiniProgram(
        activity: Activity,
        shareParams: InnerShareParams,
        shareChannel: ShareChannel
    ): ShareResult {
        //小程序不支持分享到朋友圈
        if (shareChannel != ShareChannel.WechatFriend) {
            return ShareResult(ShareEc.PlatformUnSupport)
        }
        val pageUrl = shareParams.miniProgramWebPageUrl
        val miniProgramPath = shareParams.miniProgramPath
        val userName = shareParams.miniProgramUserName
        val urlEm = pageUrl.checkEmpty("miniProgramWebPageUrl")
        val pathEm = miniProgramPath.checkEmpty("miniProgramPath")
        val userNameEm = userName.checkEmpty("miniProgramUserName")
        var em: String? = null
        if (!urlEm.isNullOrEmpty()) {
            em = urlEm
        } else if (!pathEm.isNullOrEmpty()) {
            em = pathEm
        } else if (!userNameEm.isNullOrEmpty()) {
            em = userNameEm
        }
        if (!em.isNullOrEmpty()) {
            return ShareResult(ShareEc.ParameterError, em)
        }

        val shareMiniProgramReq = getShareMiniProgramReq(activity, shareParams)
        shareMiniProgramReq.apply {
            this.scene = SendMessageToWX.Req.WXSceneSession
        }
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
                            //没返回错误码也当做成功
                            shareEc = BaseResp.ErrCode.ERR_OK
                        }
                    }

                }
            }
        })
        val sendReq = getApi(activity).sendReq(shareMiniProgramReq)
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
     * ShareImgUI           微信好友
     * ShareToTimeLineUI    微信朋友圈
     * AddFavoriteUI        微信收藏
     */
    private fun makeWechatMediaIntent(
        activity: Activity,
        shareChannel: ShareChannel,
        filePath: String,
        type: String
    ): Intent {
        val file = File(filePath)
        val intent = Intent("android.intent.action.SEND")
        val uri: Uri
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            uri = FileProvider.getUriForFile(activity, "com.uneed.yuni.fileProvider", file)
        } else {
            uri = Uri.fromFile(file)
        }
        intent.setDataAndType(uri, type)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.makePackage(shareChannel)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        return intent
    }

    private fun makeTextIntent(shareChannel: ShareChannel, text: String): Intent {
        return Intent("android.intent.action.SEND").apply {
            makePackage(shareChannel)
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
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

private fun Intent.makePackage(shareChannel: ShareChannel) {
    val pkg = "com.tencent.mm"
    if (true) {
        val cls =
            "com.tencent.mm.ui.tools.${if (shareChannel == ShareChannel.WechatMoment) "ShareToTimeLineUI" else "ShareImgUI"}"
        component = ComponentName(pkg, cls)
    } else {
        setPackage(pkg)
    }
}