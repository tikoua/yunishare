package com.tikoua.share.platform

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.modelmsg.WXImageObject
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage
import com.tencent.mm.opensdk.modelmsg.WXTextObject
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.tikoua.share.R
import com.tikoua.share.model.InnerShareParams
import com.tikoua.share.model.ShareChannel
import com.tikoua.share.model.ShareType
import com.tikoua.share.utils.Util
import com.tikoua.share.wechat.WechatShareMeta
import java.io.File
import java.util.*

/**
 *   created by dcl
 *   on 2020/8/13 10:46 AM
 */
class WechatPlatform : Platform {
    private var meta: WechatShareMeta? = null
    private var api: IWXAPI? = null
    private val thumbSize = 150

    override fun init(context: Context) {
        val api = getApi(context)
        val meta = getMeta(context)
        api.registerApp(meta.appid)
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                log(intent?.dataString)
            }

        }, IntentFilter(ConstantsAPI.ACTION_REFRESH_WXAPP))
    }

    override fun support(type: ShareChannel): Boolean {
        return type == ShareChannel.WechatFriend || type == ShareChannel.WechatMoment
    }

    override fun share(activity: Activity, type: ShareChannel, shareParams: InnerShareParams) {
        val req: SendMessageToWX.Req? =
            when (shareParams.type) {
                ShareType.Text.type -> {
                    getShareTextReq(activity, shareParams).apply {
                        if (type == ShareChannel.WechatFriend) {
                            this.scene = SendMessageToWX.Req.WXSceneSession
                        } else if (type == ShareChannel.WechatMoment) {
                            this.scene = SendMessageToWX.Req.WXSceneTimeline
                        }
                    }
                }
                ShareType.Image.type -> {
                    getShareImageReq(activity, shareParams)?.apply {
                        if (type == ShareChannel.WechatFriend) {
                            this.scene = SendMessageToWX.Req.WXSceneSession
                        } else if (type == ShareChannel.WechatMoment) {
                            this.scene = SendMessageToWX.Req.WXSceneTimeline
                        }
                    }
                }
                ShareType.Video.type -> {
                    getShareTextReq(activity, shareParams).apply {
                        if (type == ShareChannel.WechatFriend) {
                            this.scene = SendMessageToWX.Req.WXSceneSession
                        } else if (type == ShareChannel.WechatMoment) {
                            this.scene = SendMessageToWX.Req.WXSceneTimeline
                        }
                    }
                }
                else -> null
            }
        req?.let {
            getApi(activity).sendReq(req)
        }

    }

    /**
     * 分享到微信好友
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
     * 分享到微信朋友圈
     */
    private fun shareToMoment(activity: Activity, shareParams: InnerShareParams) {

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
        val appInfo = context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        )
        val metaData = appInfo.metaData
        val appid = metaData.getString("wechat_appid")
        val appSecret = metaData.getString("wechat_secret")
        val userName = metaData.getString("wechat_user_name")
        if (appid.isNullOrEmpty() || appSecret.isNullOrEmpty()) {
            throw Exception("appid or appSecret is null")
        }
        meta = WechatShareMeta(appid, appSecret)
        this.meta = meta
        return meta
    }

    private fun log(msg: String?) {
        Log.d(javaClass.simpleName, "log msg: $msg")
    }
}