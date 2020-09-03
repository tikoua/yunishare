package com.tikoua.share.wechat

import android.app.Activity
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.tencent.mm.opensdk.constants.Build
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.*
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.tikoua.share.BuildConfig
import com.tikoua.share.model.*
import com.tikoua.share.platform.Platform
import com.tikoua.share.utils.FileUtils.copyToShareTemp
import com.tikoua.share.utils.UrlUtils
import com.tikoua.share.utils.checkEmpty
import com.tikoua.share.utils.getIntOrNull
import com.tikoua.share.wechat.bean.WeChatShareRespData
import com.tikoua.share.wechat.bean.WechatAccessTokenData
import com.tikoua.share.wechat.bean.WechatAuthCodeRespData
import com.tikoua.share.wechat.bean.WechatUserInfo
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.coroutines.coroutineContext

/**
 *   created by dcl
 *   on 2020/8/13 10:46 AM
 *   api.registerApp: 未安装app时返回false
 */
class WechatPlatform : Platform {
    private var respMap = mutableMapOf<String, Bundle>()
    private var meta: WechatShareMeta? = null
    private var api: IWXAPI? = null
    override fun init(context: Context) {
        super.init(context)
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
                intent?.let {
                    val respData = it.getBundleExtra(WXConst.WXRespDataKey) ?: return@let
                    if (BuildConfig.DEBUG) {
                        respData.keySet().forEach {
                            log("data: key: $it  value:${respData[it]}")
                        }
                    }
                    val transaction = respData[WXConst.RespKeyTransaction] as String
                    respMap[transaction] = respData
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
        shareParams: ShareParams
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
            ShareType.Link.type -> {
                shareLink(activity, shareParams, shareChannel)
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

    override suspend fun auth(activity: Activity, channel: ShareChannel): AuthResult {
        val wxAppSupportAPI = getApi(activity).wxAppSupportAPI
        if (wxAppSupportAPI == 0) {
            return AuthResult(ShareEc.NotInstall)
        }
        val codeResp = getAuthCode(activity)
        val authCodeWXEc = codeResp.wxEc
        if (!codeResp.isSuccess()) {
            val ec: Int
            var em: String? = null
            when (authCodeWXEc) {
                BaseResp.ErrCode.ERR_USER_CANCEL -> {
                    ec = ShareEc.Cancel
                }
                BaseResp.ErrCode.ERR_AUTH_DENIED -> {
                    ec = ShareEc.AuthDenied
                }
                BaseResp.ErrCode.ERR_UNSUPPORT -> {
                    ec = ShareEc.PlatformUnSupport
                }
                else -> {
                    ec = ShareEc.Unknown
                    em = "Unrecognizable error code"
                }
            }
            return AuthResult(ec, em)
        }
        val authCode = codeResp.authCode!!
        val accessToken = getAccessToken(activity, authCode)
        log("accessToken: $accessToken")
        if (accessToken == null || accessToken.accessToken.isEmpty() || accessToken.openid.isEmpty()) {
            return AuthResult(ShareEc.AuthAccessTokenError)
        }
        val userInfo = getUserInfo(accessToken.accessToken, accessToken.openid)
        log("userInfo: ${userInfo}")
        if (userInfo?.nickname.isNullOrEmpty()) {
            val ec = userInfo?.errcode
            val em = userInfo?.errmsg
            var msg: String? = null
            if (ec != null || em != null) {
                msg = "error code: $ec  error msg: $em"
            }
            return AuthResult(ShareEc.AuthUserInfoError, msg)
        }
        return AuthResult(ec = ShareEc.Success, authData = userInfo)
    }


    /**
     * 生成小程序分享的req
     */
    private fun getShareMiniProgramReq(
        activity: Activity,
        shareParams: ShareParams
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
        shareParams: ShareParams,
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
        shareParams: ShareParams,
        shareChannel: ShareChannel
    ): ShareResult {
        val imagePath = shareParams.imagePath!!
        val tempPath = copyToShareTemp(activity, imagePath)
        if (tempPath.isNullOrEmpty()) {
            return ShareResult(ShareEc.CopyFileFailed)
        }
        val intent = makeWechatMediaIntent(activity, shareChannel, tempPath, "image/*")
        activity.startActivity(intent)
        return ShareResult(ShareEc.Success)
    }

    /**
     * 视频使用系统分享
     */
    private fun shareVideo(
        activity: Activity,
        shareParams: ShareParams,
        shareChannel: ShareChannel
    ): ShareResult {
        //不支持分享视频到朋友圈
        if (shareChannel != ShareChannel.WechatFriend) {
            return ShareResult(ShareEc.PlatformUnSupport)
        }
        val videoPath = shareParams.videoPath!!
        val tempPath = copyToShareTemp(activity, videoPath)
        if (tempPath.isNullOrEmpty()) {
            return ShareResult(ShareEc.CopyFileFailed)
        }
        val intent = makeWechatMediaIntent(activity, shareChannel, tempPath, "video/*")
        activity.startActivity(intent)
        return ShareResult(ShareEc.Success)
    }

    /**
     *使用sdk分享小程序到会话
     */
    private suspend fun shareMiniProgram(
        activity: Activity,
        shareParams: ShareParams,
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
        return shareBySdk(activity, shareMiniProgramReq)
    }

    /**
     * 分享链接
     */
    private suspend fun shareLink(
        activity: Activity,
        shareParams: ShareParams,
        shareChannel: ShareChannel
    ): ShareResult {
        val title = shareParams.title
        val desc = shareParams.desc
        val link = shareParams.link
        val checkLink = link.checkEmpty("link")
        if (!checkLink.isNullOrEmpty()) {
            return ShareResult(ShareEc.ParameterError, checkLink)
        }
        val thumbData = shareParams.thumbData
        val req = makeLinkReq(link!!, title, desc, thumbData)
        req.apply {
            this.scene =
                if (shareChannel == ShareChannel.WechatFriend) SendMessageToWX.Req.WXSceneSession else SendMessageToWX.Req.WXSceneTimeline
        }
        return shareBySdk(activity, req)
    }

    private fun makeLinkReq(
        link: String,
        title: String?,
        desc: String?,
        thumbData: ByteArray?
    ): SendMessageToWX.Req {
        val wxMiniProgramObject = WXWebpageObject()
        wxMiniProgramObject.webpageUrl = link
        val msg = WXMediaMessage()
        msg.mediaObject = wxMiniProgramObject
        msg.description = desc
        msg.title = title
        msg.thumbData = thumbData
        log("shareParams.thumbData: " + msg.thumbData?.size)
        val req = SendMessageToWX.Req()
        req.transaction = buildTransaction()
        req.message = msg
        return req
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
        val intent = Intent(Intent.ACTION_SEND)
        val uri: Uri
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            uri = FileProvider.getUriForFile(activity, "${activity.packageName}.fileProvider", file)
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

    /**
     * 当调用分享的页面resume之后1秒内，如果还没有收到微信的结果回调，则手动置为成功
     */
    private suspend fun waitUntilResume(activity: Activity): Boolean {
        val context = coroutineContext
        return suspendCancellableCoroutine { resum ->
            val hashCode = activity.hashCode()
            val listener = object :
                ActivityLifecycleCallback() {
                override fun onActivityResumed(activity: Activity) {
                    if (hashCode == activity.hashCode()) {
                        activity.application.unregisterActivityLifecycleCallbacks(this)
                        GlobalScope.launch(context) {
                            delay(1000)
                            resum.resumeWith(Result.success(true))
                        }
                    }
                }
            }
            activity.application.registerActivityLifecycleCallbacks(listener)
            resum.invokeOnCancellation {
                activity.application.unregisterActivityLifecycleCallbacks(listener)
            }
        }
    }

    /**
     * 使用sdk分享
     */
    private suspend fun shareBySdk(activity: Activity, req: SendMessageToWX.Req): ShareResult {
        val transaction = req.transaction
        val sendReq = getApi(activity).sendReq(req)
        if (!sendReq) {
            return ShareResult(ShareEc.NotInstall)
        }
        val result = withContext<WeChatShareRespData>(coroutineContext) {
            val resumeResult = async {
                return@async waitUntilResume(activity)
            }
            val sdkResult = async {
                return@async waitSdkShareResult(transaction)
            }
            val resp: WeChatShareRespData
            while (true) {
                if (sdkResult.isCompleted) {
                    resumeResult.cancel()
                    resp = sdkResult.await()
                    log("sdk 结果")
                    break
                }
                if (resumeResult.isCompleted) {
                    sdkResult.cancel()
                    resp = WeChatShareRespData(BaseResp.ErrCode.ERR_OK)
                    log("resume 结果")
                    break
                }
            }
            return@withContext resp
        }

        val ec = when (result.wxec) {
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

    private suspend fun waitSdkShareResult(transaction: String): WeChatShareRespData {
        while (true) {
            val resp = respMap.remove(transaction)
            if (resp != null) {
                val wxEc = (resp[WXConst.RespKeyErrorCode] as Int?) ?: BaseResp.ErrCode.ERR_OK
                return WeChatShareRespData(wxEc)
            }
            Log.v("WechatPlatform", "wait ...")
            delay(1000)
        }
    }

    /**
     * 微信授权的第一步:请求 CODE
     */
    private suspend fun getAuthCode(activity: Activity): WechatAuthCodeRespData {
        val api = getApi(activity)
        val transaction = buildTransaction()
        val req = SendAuth.Req().apply {
            scope = "snsapi_userinfo"
            this.transaction = transaction
        }

        api.sendReq(req)
        val result = withContext(coroutineContext) {
            val resumeResult = async {
                waitUntilResume(activity)
            }
            val sdkResult = async {
                waitAuthResp(transaction)
            }
            val codeResp: WechatAuthCodeRespData
            while (true) {
                if (sdkResult.isCompleted) {
                    resumeResult.cancel()
                    codeResp = sdkResult.await()
                    log("sdk 结果")
                    break
                }
                if (resumeResult.isCompleted) {
                    sdkResult.cancel()
                    codeResp = WechatAuthCodeRespData(BaseResp.ErrCode.ERR_USER_CANCEL)
                    log("resume 结果")
                    break
                }
            }
            return@withContext codeResp
        }
        return result
    }

    /**
     * 等待并解析微信返回的数据
     * 如果微信返回结果ec=ERR_OK,但是没有获取到code,则将返回的code修改为ERR_USER_CANCEL
     */
    private suspend fun waitAuthResp(transaction: String): WechatAuthCodeRespData {
        while (true) {
            val resp = respMap.remove(transaction)
            if (resp != null) {
                val wxEc = (resp[WXConst.RespKeyErrorCode] as Int?) ?: BaseResp.ErrCode.ERR_OK
                val authUrl = resp[WXConst.RespKeyAuthUrl] as String?
                val code = authUrl?.let {
                    try {
                        Uri.parse(it).getQueryParameter("code")
                    } catch (error: Throwable) {
                        null
                    }
                }
                val fixWxEc = if (code.isNullOrEmpty()) {
                    BaseResp.ErrCode.ERR_USER_CANCEL
                } else {
                    wxEc
                }
                return WechatAuthCodeRespData(fixWxEc, code)
            }
            Log.v("WechatPlatform", "wait ...")
            delay(1000)
        }
    }

    /**
     * 微信授权第二步:获取accesstoken
     */
    private suspend fun getAccessToken(activity: Activity, code: String): WechatAccessTokenData? {
        val meta = getMeta(activity)
        val appid = meta.appid
        val secret = meta.appSecret
        val url =
            "https://api.weixin.qq.com/sns/oauth2/access_token?appid=${appid}&secret=${secret}&code=${code}&grant_type=authorization_code"
        val get = withContext(Dispatchers.IO) {
            UrlUtils.get(url)
        }
        if (get != null) {
            val respStr = String(get)
            log("getAccessToken: data: $respStr ")
            val json = JSONObject(respStr)
            val accessToken = json.optString("access_token")
            if (accessToken.isNullOrEmpty()) {
                return null
            }
            val expiresIn = json.optString("expires_in")
            val refreshToken = json.optString("refresh_token")
            val openid = json.optString("openid")
            val scope = json.optString("scope")
            val unionid = json.optString("unionid")
            val errcode = json.getIntOrNull("errcode")
            val errmsg = json.optString("errmsg")
            val tokenData =
                WechatAccessTokenData(
                    accessToken,
                    expiresIn,
                    refreshToken,
                    openid,
                    scope,
                    unionid,
                    errcode,
                    errmsg
                )
            return tokenData
        }
        return null
    }

    /**
     * 微信授权第三部:获取用户信息
     */
    private suspend fun getUserInfo(accessToken: String, openid: String): WechatUserInfo? {
        val url =
            "https://api.weixin.qq.com/sns/userinfo?access_token=${accessToken}&openid=${openid}"
        val get = withContext(Dispatchers.IO) {
            UrlUtils.get(url)
        }
        if (get != null) {
            val respStr = String(get)
            log("getUserInfo: data: $respStr ")
            val json = JSONObject(respStr)
            val respOpenid: String? = json.optString("openid")
            val nickname: String? = json.optString("nickname")
            val sex: Int? = json.optInt("sex")
            val language: String? = json.optString("language")
            val city: String? = json.optString("city")
            val province: String? = json.optString("province")
            val country: String? = json.optString("country")
            val headimgurl: String? = json.optString("headimgurl")
            val privilege: JSONArray? = json.optJSONArray("privilege")
            val unionid: String? = json.optString("unionid")
            val ec: Int? = json.optInt("errcode")
            val em: String? = json.optString("errmsg")
            return WechatUserInfo(
                respOpenid,
                nickname,
                sex,
                language,
                city,
                province,
                country,
                headimgurl,
                privilege,
                unionid,
                ec,
                em
            )
        }
        return null
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