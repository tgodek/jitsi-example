package com.mangata.jitsiexample.feature_webview

import android.os.Bundle
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.mangata.jitsiexample.R
import com.mangata.jitsiexample.util.Constants
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import java.net.URL

class WebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_web_view)
        webView = findViewById(R.id.liveMeetWebView)

        val roomName = intent.getStringExtra(Constants.ROOM_NAME)

        val options = JitsiMeetConferenceOptions.Builder()
            .setServerURL(URL("https://meet.jit.si"))
            .setRoom("$roomName#config.disableDeepLinking=true")
            .build()

        val webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                runOnUiThread {
                    request?.grant(request.resources)
                }
            }
        }

        val webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                finish()
                return true
            }
        }

        webView.apply {
            settings.javaScriptEnabled = true
            // Use WideViewport if there is no viewport defined
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true

            this.webChromeClient = webChromeClient
            this.webViewClient = webViewClient
            this.loadUrl("${options.serverURL}/${options.room}")
        }
    }

    override fun onStop() {
        super.onStop()
        /**
         * When the application falls into the background we want to stop the media stream
         * such that the camera is free to use by other apps.
         */
        webView.evaluateJavascript("if(window.localStream){window.localStream.stop();}", null)
    }
}