package com.tikoua.share.utils

/**
 *   created by dcl
 *   on 2020/8/18 12:38 PM
 */
/**
 * 如果字符串为空，则返回"$name can not be empty or null"
 * 反之返回null,即:返回null表示字符串满足要求
 */
fun String?.checkEmpty(name: String): String? {
    if (this.isNullOrEmpty()) {
        return "$name can not be empty or null"
    }
    return null
}
