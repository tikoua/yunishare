package com.tikoua.share.utils

import android.util.Log

/**
 *   created by dcl
 *   on 2020/8/13 5:27 PM
 */
fun Any?.log(msg: String?, tag: String? = null) {
    Log.d(if (tag.isNullOrEmpty()) "YuniShare" else tag, msg ?: "null")
}