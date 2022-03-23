package com.mangata.jitsiexample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.webkit.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import java.net.URL

class WebViewActivity : AppCompatActivity() {


    // TODO: "CheckMediaAccessPermission: Not supported" Warnings
    // Often caused problem using WebView

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private val permissionRequest: MutableList<String> = ArrayList()
    private var isCameraPermissionGranted = false
    private var isMicrophonePermissionGranted = false

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_web_view)
        webView = findViewById(R.id.liveMeetWebView)

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                isCameraPermissionGranted =
                    permissions[Manifest.permission.CAMERA] ?: isCameraPermissionGranted
                isMicrophonePermissionGranted =
                    permissions[Manifest.permission.RECORD_AUDIO] ?: isMicrophonePermissionGranted

                if (!isCameraPermissionGranted || !isMicrophonePermissionGranted)
                    finish()
            }

        checkForAndAskForPermissions()

        val roomName = intent.getStringExtra("ROOM_NAME")

        val options = JitsiMeetConferenceOptions.Builder()
            .setServerURL(URL("https://meet.jit.si"))
            //.setConfigOverride("disableDeepLinking", "true")
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

    private fun checkForAndAskForPermissions() {
        isCameraPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        isMicrophonePermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!isCameraPermissionGranted) {
            permissionRequest.add(Manifest.permission.CAMERA)
        }
        if (!isMicrophonePermissionGranted) {
            permissionRequest.add(Manifest.permission.RECORD_AUDIO)
        }

        if (permissionRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }
    }
}