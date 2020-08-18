package com.tikoua.share

import org.junit.Test

/**
 *   created by dcl
 *   on 2020/8/18 4:36 PM
 */
class OrTest {
    @Test
    fun testOr() {
        val a = 0xb
        val b = 0x0080000
        println("a | b = ${(a or b).toString(16)}")
    }
}