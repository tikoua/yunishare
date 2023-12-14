package com.uneed.yuni

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import androidx.appcompat.app.AppCompatActivity
import com.lcw.library.imagepicker.ImagePicker
import com.tikoua.share.YuniShare
import com.tikoua.share.model.ShareChannel
import com.tikoua.share.model.ShareParams
import com.tikoua.share.system.SystemPlatform
import com.tikoua.share.system.buildSystemImage
import com.tikoua.share.system.buildSystemText
import com.tikoua.share.system.buildSystemVideo
import com.tikoua.share.utils.log
import com.tikoua.share.wechat.*
import com.tikoua.yunishare.qq.*
import com.uneed.yuni.utils.DownloadUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream


class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        GlobalScope.launch {
            repeat(100) {
                pv.setProgress(it + 1)
                delay(500)
            }
        }
        bt.setOnClickListener {
            testExten()
        }
        btClear.setOnClickListener {
            edt.setText(null)
            tvResult.text = null
        }

        SystemPlatform().apply { this.init(this@MainActivity) }
        WechatPlatform().apply { this.init(this@MainActivity) }
        QQPlatform().apply { this.init(this@MainActivity) }

        btWechatFriendText.setOnClickListener(this)
        btWechatFriendImage.setOnClickListener(this)
        btWechatFriendVideo.setOnClickListener(this)
        btWechatFriendMiniProgram.setOnClickListener(this)
        btWechatFriendLink.setOnClickListener(this)
        btWechatMomentImage.setOnClickListener(this)
        btWechatMomentLink.setOnClickListener(this)
        btQQText.setOnClickListener(this)
        btQQLocalImage.setOnClickListener(this)
        btQQVideo.setOnClickListener(this)
        btQQLink.setOnClickListener(this)
        btQZoneText.setOnClickListener(this)
        btQZoneLocalImage.setOnClickListener(this)
        btQZoneVideo.setOnClickListener(this)
        btQZoneLink.setOnClickListener(this)
        btSystemText.setOnClickListener(this)
        btSystemLocalImage.setOnClickListener(this)
        btSystemVideo.setOnClickListener(this)
        btAuthWechat.setOnClickListener(this)
        btAuthQQ.setOnClickListener(this)
        btAuthAlipay.setOnClickListener(this)
        btWechatMomentText.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        val text = "分享的文本"
        val imageUrl = "https://cdn.uneed.com/download/box_string200810.jpg"
        val videoUrl =
            "https://cdn.uneed.com/download/0bf2oeaaiaaa4yaob6igj5pva4odaryqabaa.f10003.mp4"
        val pageUrl = "https://www.rockmessenger.com/index.html"
        val path = "pages/index/main?uid=124287543079337984&key=491209839083520000"
        val title = "海非深64邀请你成为好友"
        val miniCover = "https://cdn.uneed.com/web/icon/add_friends.png"
        val linkTitle = "与你App - 强大的聊天体验"
        val linkDesc = "对话怼出新趣味，同屏玩法层出不穷，集齐交友、聊天、云相册的个性化社交app~"
        val urlGet = "https://uneed.com/get"
        val imgLog = "https://cdn.uneed.com/logo_box.png"
        GlobalScope.launch {
            when (v) {
                btWechatFriendText -> testShareWechatFriendText(text)
                btWechatFriendImage -> testShareWechatFriendImage(imageUrl)
                btWechatFriendVideo -> testShareWechatFriendVideo(videoUrl)
                btWechatFriendMiniProgram -> testShareWechatFriendMiniprogram(
                    pageUrl,
                    path,
                    title,
                    miniCover
                )
                btWechatFriendLink -> testShareWechatFriendLink(urlGet, linkTitle, linkDesc, imgLog)
                btWechatMomentImage -> testShareWechatMomentImage(imageUrl)
                btWechatMomentLink -> testShareWechatMomentLink(urlGet, linkTitle, linkDesc, imgLog)
                btWechatMomentText -> testShareWechatText(text)
                btQQText -> testShareQQText(text)
                btQQLocalImage -> {
                    testShareQQImage(imageUrl)
                }
                btQQVideo -> {
                    testShareQQLocalVideo()
                }
                btQQLink -> testShareQQLink(urlGet, linkTitle, linkDesc, imgLog)
                btQZoneText -> testShareQZoneText(text)
                btQZoneLocalImage -> testShareQZoneImage()
                btQZoneVideo -> testShareQZoneVideo()
                btQZoneLink -> {
                    testShareQZoneLink(urlGet, linkTitle, linkDesc, imgLog)
                }
                btSystemText -> {
                    systemText(text)
                }
                btSystemLocalImage -> {
                    systemImage()
                }
                btSystemVideo -> {
                    systemVideo()
                }
                btAuthWechat -> {
                    authWechat()
                }
                btAuthQQ -> {

                }
                btAuthAlipay -> {

                }
            }
        }
    }


    private fun testExten() {
        val text = edt.text.toString()
        val encode1 = Uri.encode(text, "/:")
        val fileExtensionFromUrl =
            MimeTypeMap.getFileExtensionFromUrl(encode1)
        tvResult.setText(fileExtensionFromUrl)
    }

    private suspend fun testShareWechatFriendText(text: String) {
        YuniShare.share(
            this,
            ShareChannel.WechatFriend,
            ShareParams.buildWechatText().text(text).build()
        ).apply {
            log("share result: $this")
        }

    }

    private suspend fun testShareWechatFriendImage(imageUrl: String) {
        log("testShareImage 1")
        val imagePath = pickImage()
        log("testShareImage 2  imagePath: $imagePath")
        if (imagePath.isNullOrEmpty()) {
            return
        }
        log("imagePath: $imagePath")
        YuniShare.share(
            this@MainActivity,
            ShareChannel.WechatFriend,
            ShareParams.buildWechatImage().imagePath(imagePath).build()
        ).apply {
            log("share result: $this")
        }
    }

    private suspend fun testShareWechatFriendVideo(videoUrl: String) {
        log("testShareWechatFriendVideo: $videoUrl")
        val filePath = pickVideo()
        YuniShare.share(
            this@MainActivity,
            ShareChannel.WechatFriend,
            ShareParams.buildWechatVideo().videoPath(filePath).build()
        ).apply {
            log("share result: $this")
        }
    }

    private suspend fun testShareWechatFriendMiniprogram(
        pageUrl: String,
        path: String,
        title: String,
        miniCover: String
    ) {
        val thumb = DownloadUtils.downloadSmall(miniCover, 5000)
        log("thumb: ${thumb?.size}")
        val appInfo = this.packageManager.getApplicationInfo(
            this.packageName,
            PackageManager.GET_META_DATA
        )
        val metaData = appInfo.metaData
        val userName = metaData.getString("wechat_user_name")
        YuniShare.share(
            this,
            ShareChannel.WechatFriend,
            ShareParams.buildMiniProgram()
                .path(path)
                .webPageUrl(pageUrl)
                .thumbData(thumb)
                .userName(userName)
                .title(title)
                .build()
        ).apply {
            log("share result: $this")
        }

    }

    private suspend fun testShareWechatFriendLink(
        urlGet: String,
        linkTitle: String,
        linkDesc: String,
        imgLog: String
    ) {
        val filePath = getFilePath("save/img/imglogo.png", imgLog)
        val thumb = filePath?.let {
            withContext(Dispatchers.IO) {
                return@withContext FileInputStream(filePath).use { fis ->
                    val bytes = ByteArray(fis.available())
                    fis.read(bytes)
                    return@use bytes
                }
            }
        }

        YuniShare.share(
            this@MainActivity,
            ShareChannel.WechatFriend,
            ShareParams.buildWechatLink().title(linkTitle).desc(linkDesc).thumbData(thumb)
                .link(urlGet).build()
        ).apply {
            log("share result: $this")
        }

    }


    private suspend fun testShareWechatMomentImage(imageUrl: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                log("testShareImage 1")
                val imagePath = pickImage()

                log("testShareImage 2  imagePath: $imagePath")
                if (imagePath.isNullOrEmpty()) {
                    return@launch
                }
                log("imagePath: $imagePath")
                YuniShare.share(
                    this@MainActivity,
                    ShareChannel.WechatMoment,
                    ShareParams.buildWechatImage().imagePath(imagePath).build()
                ).apply {
                    log("share result: $this")
                }
            } catch (error: Throwable) {
                error.printStackTrace()
            }
        }
    }


    private suspend fun testShareWechatMomentLink(
        urlGet: String,
        linkTitle: String,
        linkDesc: String,
        imgLog: String
    ) {
        val filePath = getFilePath("save/img/imglogo.png", imgLog)
        val thumb = filePath?.let {
            withContext(Dispatchers.IO) {
                return@withContext FileInputStream(filePath).use { fis ->
                    val bytes = ByteArray(fis.available())
                    fis.read(bytes)
                    return@use bytes
                }
            }
        }

        YuniShare.share(
            this@MainActivity,
            ShareChannel.WechatMoment,
            ShareParams.buildWechatLink().title(linkTitle).desc(linkDesc).thumbData(thumb)
                .link(urlGet).build()
        ).apply {
            log("share result: $this")
        }
    }

    private suspend fun testShareWechatText(text: String) {
        YuniShare.share(
            this,
            ShareChannel.WechatMoment,
            ShareParams.buildWechatText().text(text).build()
        ).apply {
            log("share result: $this")
        }
    }

    private suspend fun testShareQQText(text: String) {
        YuniShare.share(
            this,
            ShareChannel.QQFriend,
            ShareParams.buildQQText().text(text)
                .targetUrl("https://www.baidu.com").build()
        ).apply {
            log("share result: $this")
        }
    }

    private suspend fun testShareQQImage(imageUrl: String) {
        try {
            log("testShareImage 1")
            val imagePath = pickImage()

            log("testShareImage 2  imagePath: $imagePath")
            if (imagePath.isNullOrEmpty()) {
                return
            }
            log("imagePath: $imagePath")
            YuniShare.share(
                this@MainActivity,
                ShareChannel.QQFriend,
                ShareParams.buildQQImage().imagePath(imagePath).build()
            ).apply {
                log("share result: $this")
            }
        } catch (error: Throwable) {
            error.printStackTrace()
        }
    }

    private suspend fun testShareQQLocalVideo() {
        try {
            log("testShareQQRemoteVideo 1")
            val imagePath = pickVideo()

            log("testShareQQRemoteVideo 2  imagePath: $imagePath")
            if (imagePath.isNullOrEmpty()) {
                return
            }
            log("testShareQQRemoteVideo: $imagePath")
            YuniShare.share(
                this@MainActivity,
                ShareChannel.QQFriend,
                ShareParams.buildQQVideo().videoPath(imagePath).build()
            ).apply {
                log("share result: $this")
            }
        } catch (error: Throwable) {
            error.printStackTrace()
        }
    }

    private suspend fun testShareQQLink(
        urlGet: String,
        linkTitle: String,
        linkDesc: String,
        imgLog: String
    ) {
        YuniShare.share(
            this@MainActivity,
            ShareChannel.QQFriend,
            ShareParams.buildQQLink().title(linkTitle).desc(linkDesc).cover(imgLog)
                .link(urlGet).build()
        ).apply {
            log("share result: $this")
        }
    }

    private suspend fun testShareQZoneText(text: String) {
        YuniShare.share(
            this,
            ShareChannel.QQZone,
            ShareParams.buildQQText().text(text).build()
        ).apply {
            log("share result: $this")
        }
    }

    private suspend fun testShareQZoneImage() {
        try {
            log("testShareImage 1")
            val imagePath = pickImage()

            log("testShareImage 2  imagePath: $imagePath")
            if (imagePath.isNullOrEmpty()) {
                return
            }
            log("imagePath: $imagePath")
            YuniShare.share(
                this@MainActivity,
                ShareChannel.QQZone,
                ShareParams.buildQQImage().imagePath(imagePath).build()
            ).apply {
                log("share result: $this")
            }
        } catch (error: Throwable) {
            error.printStackTrace()
        }
    }


    private suspend fun testShareQZoneVideo() {
        try {
            log("testShareQQRemoteVideo 1")
            val imagePath = pickVideo()

            log("testShareQQRemoteVideo 2  imagePath: $imagePath")
            if (imagePath.isNullOrEmpty()) {
                return
            }
            log("testShareQQRemoteVideo: $imagePath")
            YuniShare.share(
                this@MainActivity,
                ShareChannel.QQZone,
                ShareParams.buildQQVideo().videoPath(imagePath).build()
            ).apply {
                log("share result: $this")
            }
        } catch (error: Throwable) {
            error.printStackTrace()
        }
    }

    private suspend fun testShareQZoneLink(
        urlGet: String,
        linkTitle: String,
        linkDesc: String,
        imgLog: String
    ) {
        YuniShare.share(
            this@MainActivity,
            ShareChannel.QQZone,
            ShareParams.buildQQLink().title(linkTitle).desc(linkDesc).cover(imgLog)
                .link(urlGet).build()
        ).apply {
            log("share result: $this")
        }
    }

    private suspend fun systemText(text: String) {
        YuniShare.share(
            this@MainActivity,
            ShareChannel.System,
            ShareParams.buildSystemText().text(text).build()
        ).apply {
            log("share result: $this")
        }
    }

    private suspend fun systemImage() {
        val pickImage = pickImage() ?: return
        YuniShare.share(
            this@MainActivity,
            ShareChannel.System,
            ShareParams.buildSystemImage().imagePath(pickImage).build()
        ).apply {
            log("share result: $this")
        }
    }

    private suspend fun systemVideo() {
        val pickVideo = pickVideo() ?: return
        YuniShare.share(
            this@MainActivity,
            ShareChannel.System,
            ShareParams.buildSystemVideo().videoPath(pickVideo).build()
        ).apply {
            log("share result: $this")
        }
    }

    private suspend fun authWechat() {
        YuniShare.auth(this, ShareChannel.WechatFriend).apply {
            log("share result: $this")
        }
    }

    private suspend fun downloadFile(path: String, url: String): String? {
        val download = DownloadUtils.download(url, path)
        return if (download) path else null
    }

    private suspend fun getFilePath(subPath: String, url: String): String? {
        val filesDir = externalCacheDir
        val file = File(filesDir, subPath)
        if (file.exists() && file.isFile) {
            return file.absolutePath
        }
        return downloadFile(file.absolutePath, url)
    }

    private suspend fun pickImage(): String? {
        return pickMedia()
    }

    private suspend fun pickVideo(): String? {
        return pickMedia()
    }

    private val REQUEST_SELECT_IMAGES_CODE = 1001
    private var pickFile: String? = null
    private suspend fun pickMedia(): String? {
        pickFile = null
        ImagePicker.getInstance()
            .setTitle("标题") //设置标题
            .showCamera(false) //设置是否显示拍照按钮
            .showImage(true) //设置是否展示图片
            .showVideo(true) //设置是否展示视频
            .setSingleType(true) //设置图片视频不能同时选择
            .setMaxCount(9) //设置最大选择图片数目(默认为1，单选)
            .start(
                this@MainActivity,
                REQUEST_SELECT_IMAGES_CODE
            )
        while (pickFile == null) {
            delay(1000)
        }
        val path = pickFile
        return if (path.isNullOrEmpty()) null else path
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT_IMAGES_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                data?.let {
                    val img = data.getStringArrayListExtra(ImagePicker.EXTRA_SELECT_IMAGES)
                    pickFile = img?.firstOrNull()
                }
            } else {
                pickFile = ""
            }
        }
    }


}
