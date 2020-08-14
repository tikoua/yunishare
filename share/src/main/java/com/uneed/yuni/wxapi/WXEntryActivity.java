package com.uneed.yuni.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tikoua.share.wechat.WXConst;
import com.tikoua.share.wechat.WechatShareMeta;
import com.tikoua.share.wechat.WechatUtilKt;

import java.lang.ref.WeakReference;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    private static String TAG = "MicroMsg.WXEntryActivity";

    private IWXAPI api;
    private MyHandler handler;

    private static class MyHandler extends Handler {
        private final WeakReference<WXEntryActivity> wxEntryActivityWeakReference;

        public MyHandler(WXEntryActivity wxEntryActivity) {
            wxEntryActivityWeakReference = new WeakReference<WXEntryActivity>(wxEntryActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            int tag = msg.what;
            Log.d("MyHandler", "handleMessage tag: " + tag);
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
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MyHandler", "onCreate");
        WechatShareMeta wechatMeta = WechatUtilKt.getWechatMeta(this);
        api = WXAPIFactory.createWXAPI(this, wechatMeta.getAppid(), false);
        handler = new MyHandler(this);

        try {
            Intent intent = getIntent();
            api.handleIntent(intent, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("MyHandler", "onNewIntent");
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    /**
     * 微信发送的请求将回调到 onReq 方法
     *
     * @param req
     */
    @Override
    public void onReq(BaseReq req) {
        Log.d("MyHandler", "onReq");
        switch (req.getType()) {
            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
                break;
            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
                break;
            default:
                break;
        }
        finish();
    }

    /**
     * 发送到微信请求的响应结果将回调到 onResp 方法
     *
     * @param resp
     */
    @Override
    public void onResp(BaseResp resp) {
        int errCode = resp.errCode;
        int type = resp.getType();
        Log.d("MyHandler", "onResp  errCode: " + errCode + "  type: " + type);
        Intent intent = new Intent(WXConst.ActionWXResp);
        intent.putExtra("ec", errCode);
        sendBroadcast(intent);
        finish();
    }
}