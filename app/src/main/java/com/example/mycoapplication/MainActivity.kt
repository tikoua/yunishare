package com.example.mycoapplication

import android.net.Uri
import android.os.Bundle
import android.webkit.MimeTypeMap
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

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
            val text = edt.text.toString()
            val encode1 = Uri.encode(text, "/:")
            val fileExtensionFromUrl =
                MimeTypeMap.getFileExtensionFromUrl(encode1)
            tvResult.setText(fileExtensionFromUrl)
        }
        btClear.setOnClickListener {
            edt.setText(null)
            tvResult.text = null
        }
    }
}
