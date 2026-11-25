package com.yiomigs.chesscoach

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient

class MainActivity : Activity() {
    private lateinit var web: WebView
    private val overlayRequest = 4101
    private val audioRequest = 4102

    @Suppress("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.allowFileAccess = true
        web.webChromeClient = WebChromeClient()
        web.webViewClient = WebViewClient()
        web.addJavascriptInterface(Bridge(), "Android")
        setContentView(web)
        web.loadUrl("file:///android_asset/index.html")
    }

    inner class Bridge {
        @JavascriptInterface fun requestOverlay() {
            runOnUiThread {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    val i = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                    startActivityForResult(i, overlayRequest)
                } else startOverlay()
            }
        }
        @JavascriptInterface fun pickAudio() {
            runOnUiThread {
                val i = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "audio/*"
                }
                startActivityForResult(i, audioRequest)
            }
        }
        @JavascriptInterface fun stopOverlay() {
            stopService(Intent(this@MainActivity, FloatingMenuService::class.java))
        }
    }

    private fun startOverlay() {
        if (Settings.canDrawOverlays(this)) {
            startService(Intent(this, FloatingMenuService::class.java))
            web.evaluateJavascript("overlayStatus('Плавающее меню включено')", null)
        }
    }

    @Deprecated("Deprecated in Android")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == overlayRequest && Settings.canDrawOverlays(this)) startOverlay()
        if (requestCode == audioRequest && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startService(Intent(this, FloatingMenuService::class.java).apply {
                    action = FloatingMenuService.ACTION_AUDIO
                    putExtra(FloatingMenuService.EXTRA_URI, uri.toString())
                })
                web.evaluateJavascript("audioChosen()", null)
            }
        }
    }

    @Deprecated("Deprecated in Android")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}