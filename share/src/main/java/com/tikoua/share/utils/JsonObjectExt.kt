package com.tikoua.share.utils

import org.json.JSONObject

/**
 *   created by dcl
 *   on 2020/8/21 10:53 AM
 */
fun JSONObject.getIntOrNull(name: String): Int? {
    if (has(name)) {
        try {
            return getInt(name)
        } catch (error: Throwable) {
            return null
        }
    }
    return null
}