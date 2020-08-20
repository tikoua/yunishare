package com.tikoua.share.wechat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.tikoua.share.utils.log
import com.uneed.yuni.BuildConfig
import java.lang.ref.WeakReference

/**
 *   created by dcl
 *   on 2020/8/20 12:00 PM
 */
abstract class WechatHandlerActivity : Activity(), IWXAPIEventHandler {
    private lateinit var api: IWXAPI
    private var handler: MyHandler? = null

    class MyHandler(wxEntryActivity: WechatHandlerActivity) :
        Handler() {
        private val wxEntryActivityWeakReference: WeakReference<WechatHandlerActivity>
        override fun handleMessage(msg: Message) {
            val tag = msg.what
            Log.d("MyHandler", "handleMessage tag: $tag")
            /*switch (tag) {
                case NetworkUtil.GET_TOKEN: {
                    Bundle data = msg.getData();
                    JSONObject json = null;
                    try {
                        json = new JSONObject(data.getString("result"));
                        String openId, accessToken, refreshToken, scope;
                        openId = json.getString("openid");
                        accessToken = json.getString("access_token");
                        refreshToken = json.getString("refresh_token");
                        scope = json.getString("scope");
                        Intent intent = new Intent(wxEntryActivityWeakReference.get(), SendToWXActivity.class);
                        intent.putExtra("openId", openId);
                        intent.putExtra("accessToken", accessToken);
                        intent.putExtra("refreshToken", refreshToken);
                        intent.putExtra("scope", scope);
                        wxEntryActivityWeakReference.get().startActivity(intent);
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }*/
        }

        init {
            wxEntryActivityWeakReference =
                WeakReference<WechatHandlerActivity>(wxEntryActivity)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log("onCreate")
        val (appid) = this.loadWechatMeta()
        api = WXAPIFactory.createWXAPI(this, appid, false)
        handler = MyHandler(this)
        try {
            val intent: Intent = getIntent()
            api.handleIntent(intent, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        log("onNewIntent")
        setIntent(intent)
        api.handleIntent(intent, this)
    }

    /**
     * 微信发送的请求将回调到 onReq 方法
     *
     * @param req
     */
    override fun onReq(req: BaseReq) {
        log("onReq")
        when (req.type) {
            ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX -> {
            }
            ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX -> {
            }
            else -> {
            }
        }
        finish()
    }

    /**
     * 发送到微信请求的响应结果将回调到 onResp 方法
     *
     * @param resp
     */
    override fun onResp(resp: BaseResp) {
        val errCode = resp.errCode
        val bundle = Bundle()
        resp.toBundle(bundle)
        val type = resp.type
        if (BuildConfig.DEBUG) {
            log("onResp  errCode: $errCode  type: $type resp: $bundle")
        }
        val intent = Intent(WXConst.ActionWXResp)
        intent.putExtra("ec", errCode)
        intent.putExtra(WXConst.WXRespDataKey, bundle)
        sendBroadcast(intent)
        finish()
    }

    abstract fun onGetMessageFromWXReq(msg: Any?)
    abstract fun onShowMessageFromWXReq(msg: Any?)
}