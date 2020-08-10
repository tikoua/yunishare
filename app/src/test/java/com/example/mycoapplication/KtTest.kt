package com.example.mycoapplication

import android.net.Uri
import org.junit.Test
import java.net.URI
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder

class KtTest {
    @Test
    fun main() {
        val url = "http://xiazai.suanmiao-zuida.com/2007/泡澡少女-01.mp4"
        val encode = URLEncoder.encode(url, Charsets.UTF_8.name())
        val decode = URLDecoder.decode(encode, Charsets.UTF_8.name())


        println("encode : ${encode}")
        println("decode : ${decode}")

    }
}
