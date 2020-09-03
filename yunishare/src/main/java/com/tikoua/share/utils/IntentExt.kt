package com.tikoua.share.utils

import android.content.Context
import android.content.Intent

/**
 *   created by dcl
 *   on 2020/9/3 15:18
 */
fun Intent.toChooser(context: Context): Intent {
    return Intent.createChooser(
        this,
        context.applicationInfo.loadDescription(context.packageManager) ?: ""
    )
}