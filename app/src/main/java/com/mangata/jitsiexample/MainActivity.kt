package com.mangata.jitsiexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mangata.jitsiexample.databinding.ActivityMainBinding
import com.mangata.jitsiexample.feature_embedded.EmbeddedActivity
import com.mangata.jitsiexample.feature_webview.WebViewActivity
import com.mangata.jitsiexample.util.Constants
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import java.net.URL

@RequiresApi(Build.VERSION_CODES.R)
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isCameraPermissionGranted = false
    private var isMicrophonePermissionGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

                isCameraPermissionGranted =
                    permissions[Manifest.permission.CAMERA] ?: isCameraPermissionGranted
                isMicrophonePermissionGranted =
                    permissions[Manifest.permission.RECORD_AUDIO] ?: isMicrophonePermissionGranted
            }

        checkForAndAskForPermissions()

        binding.apply {
            nativeJoinButton.setOnClickListener {
                if (validInput(roomNameEditTxt))
                    startNativeView(roomNameEditTxt.text.toString())
            }

            webViewJoinButton.setOnClickListener {
                if (validInput(roomNameEditTxt))
                    startWebView(roomNameEditTxt.text.toString())
            }

            embededJoinButton.setOnClickListener {
                if (validInput(roomNameEditTxt))
                    startEmbeddedView(roomNameEditTxt.text.toString())
            }
        }
    }

    private fun startWebView(roomName: String) {
        val intent = Intent(this, WebViewActivity::class.java).apply {
            putExtra(Constants.ROOM_NAME, roomName)
        }
        startActivity(intent)
    }

    private fun startEmbeddedView(roomName: String) {
        val intent = Intent(this, EmbeddedActivity::class.java).apply {
            putExtra(Constants.ROOM_NAME, roomName)
        }
        startActivity(intent)
    }

    private fun startNativeView(roomName: String) {
        val options = JitsiMeetConferenceOptions.Builder()
            .setServerURL(URL("https://meet.jit.si"))
            .setRoom(roomName)
            .build()

        JitsiMeetActivity.launch(this, options)
    }

    private fun validInput(roomName: EditText): Boolean {
        if (roomName.text.isEmpty()) {
            roomName.error = "Enter room name!"
            return false
        }
        if (!permissionsGranted()) {
            checkForAndAskForPermissions()
            return false
        }
        return true
    }

    private fun permissionsGranted(): Boolean =
        isMicrophonePermissionGranted && isCameraPermissionGranted

    private fun checkForAndAskForPermissions() {
        val permissionRequest: MutableList<String> = ArrayList()

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