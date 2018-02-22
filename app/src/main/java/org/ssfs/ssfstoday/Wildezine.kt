package org.ssfs.ssfstoday

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView

class Wildezine : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_wildezine)
        val webview = WebView(this)
        setContentView(webview)

        webview.loadUrl("https://wildezine.com")

    }
}
