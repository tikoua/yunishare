package com.tikoua.share.model

import com.tikoua.share.wechat.WechatImageBuilder
import com.tikoua.share.wechat.WechatMiniProgramBuilder
import com.tikoua.share.wechat.WechatTextBuilder
import com.tikoua.share.wechat.WechatVideoBuilder

/**
 *   created by dcl
 *   on 2020/8/10 6:04 PM
 */
class InnerShareParams {
    companion object {
        fun buildWechatText(): WechatTextBuilder {
            return WechatTextBuilder()
        }

        fun buildWechatImage(): WechatImageBuilder {
            return WechatImageBuilder()
        }

        fun buildWechatVideo(): WechatVideoBuilder {
            return WechatVideoBuilder()
        }

        fun buildMiniProgram(): WechatMiniProgramBuilder {
            return WechatMiniProgramBuilder()
        }
    }

    /**
     * [ShareType]
     */
    internal var type: Int = 0
    internal var text: String? = null
    internal var imagePath: String? = null
    internal var title: String? = null
    internal var desc: String? = null
    internal var videoUrl: String? = null
    internal var videoLowBandUrl: String? = null
    internal var miniProgramWebPageUrl: String? = null
    internal var miniProgramUserName: String? = null
    internal var miniProgramPath: String? = null
    internal var miniProgramWithShareTicket: Boolean = true
    internal var miniProgramType: Int? = null
    internal var thumbData: ByteArray? = null

    internal var IMAGE_FILE_PROVIDER_PATH = "image_provider_path"
    internal var FILE_PATH = "filePath"
    internal var NOTEBOOK = "notebook"
    internal var STACK = "stack"
    internal var TAGS = "tags"
    internal var IS_PUBLIC = "isPublic"
    internal var IS_FRIEND = "isFriend"
    internal var IS_FAMILY = "isFamily"
    internal var SAFETY_LEVEL = "safetyLevel"
    internal var CONTENT_TYPE = "contentType"
    internal var HIDDEN = "hidden"
    internal var VENUE_NAME = "venueName"
    internal var VENUE_DESCRIPTION = "venueDescription"
    internal var LINKEDIN_DESCRIPTION = "linkedinDescription"
    internal var LATITUDE = "latitude"
    internal var LONGITUDE = "longitude"
    internal var IMAGE_URL = "imageUrl"
    internal var COMMENT = "comment"
    internal var TITLE_URL = "titleUrl"
    internal var URL = "url"
    internal var ADDRESS = "address"
    internal var SITE = "site"
    internal var SITE_URL = "siteUrl"
    internal var GROPU_ID = "groupID"
    internal var EXT_INFO = "extInfo"
    internal var SHARE_TYPE = "shareType"
    internal var MUSIC_URL = "musicUrl"
    internal var IMAGE_DATA = "imageData"
    internal var AUTHOR = "author"
    internal var SCENCE = "scene"
    internal var CUSTOM_FLAG = "customFlag"
    internal var EXECUTE_URL = "executeUrl"
    internal var INSTALL_URL = "installUrl"
    internal var IS_SHARE_TENCENT_WEIBO = "isShareTencentWeibo"
    internal var IMAGE_ARRAY = "imageArray"
    internal var WX_MINIPROGRAM_USER_NAME = "wxUserName"
    internal var WX_MINIPROGRAM_PATH = "wxPath"
    internal var WX_MINIPROGRAM_WITH_SHARETICKET = "wxWithShareTicket"
    internal var WX_MINIPROGRAM_MINIPROGRAM_TYPE = "wxMiniProgramType"
    internal var IS_LOG_EVEN = "isLogEven"
    internal var SUBREDDIT = "sr"
    internal var VIDEO_ARRAY = "videoArray"
    internal var ACTIVITY = "activity"
    internal var LC_SUMMARY = "lc_summary"
    internal var LC_IMAGE = "lc_image"
    internal var LC_OBJECT_TYPE = "lc_object_type"
    internal var LC_DISPLAY_NAME = "lc_display_name"
    internal var LC_CREATE_AT = "lc_create_at"
    internal var LC_URL = "lc_url"
    internal var QUOTE = "QUOTE"
    internal var HASHTAG = "HASHTAG"
    internal var QQ_MINI_PROGRAM_APPID = "mini_program_appid"
    internal var QQ_MINI_PROGRAM_PATH = "mini_program_path"
    internal var QQ_MINI_PROGRAM_TYPE = "mini_program_type"
    internal var LOOPSHARE_PARAMS_MOBID = "loopshare_params_mobid"
    internal var IMAGE_URL_LIST = "imageUrlList"
    internal var IMAGE_URI_LIST = "imageUriList"
    internal var VIDEO_URI_OASIS = "video_uri_oasis"
    internal var VIDEO_PATH_OASIS = "video_path_oasis"
    internal var FILE_IMAGE = "file_image"
    internal var FILE_VIDEO = "file_video"
    internal var FILE_STICKER = "file_sticker"
    internal var VIDEO_URI = "video_uri"
    internal var KAKAO_TEMPLATE_WEBURL = "kakao_template_weburl"
    internal var KAKAO_TEMPLATE_MOBILEWEBURL = "kakao_template_mobileweburl"
    internal var KAKAO_TEMPLATE_LIKECOUNT = "kakao_template_likecount"
    internal var KAKAO_TEMPLATE_COMMENTCOUNT = "kakao_template_commentcount"
    internal var KAKAO_TEMPLATE_SHARECOUNT = "kakao_template_sharecount"
    internal var KAKAO_TEMPLATE_ADDBUTTON_WEBURL = "kakao_template_button_weburl"
    internal var KAKAO_TEMPLATE_ADDBUTTON_MOBILEWEBURL =
        "kaokao_template_button_mobileweburl"
    internal var KAKAO_TEMPLATE_ADDBUTTON_TITLE = "kakao_template_button_title"
    internal var KAKAO_TEMPLATE_REGULARPRICE = "kaokao_template_regularprice"
    internal var KAKAO_TEMPLATE_PRODUCTNAME = "kakao_template_productname"
    internal var KAKAO_TEMPLATE_DISCOUNTPRICE = "kakao_template_discountprice"
    internal var KAKAO_TEMPLATE_DISCOUNTRATE = "kakao_template_discountrate"
    internal var KAKAO_CUSTOM_TEMPLATE = "kaokao_custom_template"
    internal var KAKAO_CUSTOM_TEMPLATEID = "kakao_custom_templateid"
    internal var WX_SCENE = "wx_scene"
    internal var WX_TEMPLATEID = "wx_templateid"
    internal var WX_RESERVED = "wx_reserved"
}