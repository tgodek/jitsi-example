package com.mangata.jitsiexample

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mangata.jitsiexample.databinding.ActivityMainBinding
import com.mangata.jitsiexample.feature_embedded.EmbeddedActivity
import com.mangata.jitsiexample.feature_webview.WebViewActivity
import com.mangata.jitsiexample.util.Constants
import org.jitsi.meet.sdk.BroadcastEvent
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import java.net.URL


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isCameraPermissionGranted = false
    private var isMicrophonePermissionGranted = false

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            onBroadcastReceived(intent)
        }
    }

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
        registerForBroadcastMessages()

        binding.apply {
            nativeJoinButton.setOnClickListener {
                if (roomNameEditTxt.text.isNotEmpty()) {
                    startCall(roomNameEditTxt.text.toString())
                } else {
                    roomNameEditTxt.error = "Enter room name!"
                }
            }

            webViewJoinButton.setOnClickListener {
                if (roomNameEditTxt.text.isNotEmpty()) {
                    if(permissionsGranted())
                        startWebView(roomNameEditTxt.text.toString())
                    else
                        checkForAndAskForPermissions()
                } else {
                    roomNameEditTxt.error = "Enter room name!"
                }
            }

            embededJoinButton.setOnClickListener {
                if (roomNameEditTxt.text.isNotEmpty()) {
                    if(permissionsGranted())
                        startEmbeddedView(roomNameEditTxt.text.toString())
                    else
                        checkForAndAskForPermissions()
                } else {
                    roomNameEditTxt.error = "Enter room name!"
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }

    private fun startWebView(roomName: String) {
        val intent = Intent(this, WebViewActivity::class.java).apply {
            putExtra(Constants.ROOM_NAME, roomName)
        }
        startActivity(intent)
    }

    private fun startEmbeddedView(roomName: String) {
        val intent = Intent(this, EmbeddedActivity::class.java).apply {
            putExtra("ROOM_NAME", roomName)
        }
        startActivity(intent)
    }

    private fun startCall(roomName: String) {
        val options = JitsiMeetConferenceOptions.Builder()
            .setServerURL(URL("https://meet.jit.si"))
            .setRoom(roomName)
            .build()

        JitsiMeetActivity.launch(this, options)
    }

    private fun registerForBroadcastMessages() {
        val intentFilter = IntentFilter()

        for (type in BroadcastEvent.Type.values()) {
            intentFilter.addAction(type.action)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun onBroadcastReceived(intent: Intent?) {
        intent?.let {
            val event = BroadcastEvent(it)
            when (event.type) {
                BroadcastEvent.Type.CONFERENCE_JOINED -> println("Conference Joined with url ${event.data["url"]}")
                BroadcastEvent.Type.PARTICIPANT_JOINED -> println("Participant joined ${event.data["name"]}")
                else -> return
            }
        }
    }

    private fun permissionsGranted() : Boolean = isMicrophonePermissionGranted && isCameraPermissionGranted

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