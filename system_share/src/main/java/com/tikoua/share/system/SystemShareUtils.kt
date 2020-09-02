package com.tikoua.share.system

import com.tikoua.share.model.ShareParams

/**
 *   created by dcl
 *   on 2020/9/2 21:47
 */
fun ShareParams.Companion.buildSystemText(): SystemTextBuilder {
    return SystemTextBuilder()
}

fun ShareParams.Companion.buildSystemImage(): SystemImageBuilder {
    return SystemImageBuilder()
}

fun ShareParams.Companion.buildSystemVideo(): SystemVideoBuilder {
    return SystemVideoBuilder()
}