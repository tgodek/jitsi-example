package com.mangata.jitsiexample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.jitsi.meet.sdk.BroadcastEvent
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import java.net.URL


class MainActivity : AppCompatActivity() {

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            onBroadcastReceived(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val joinButton = findViewById<Button>(R.id.nativeJoinButton)
        val joinWebButton = findViewById<Button>(R.id.webViewJoinButton)
        val roomName = findViewById<EditText>(R.id.roomNameEditTxt)

        registerForBroadcastMessages()

        joinButton.setOnClickListener {
            if (roomName.text.isNotEmpty()) {
                //Native approach
                startCall(roomName.text.toString())
            } else {
                roomName.error = "Enter room name!"
            }
        }

        joinWebButton.setOnClickListener {
            if (roomName.text.isNotEmpty()) {
                //WebView approach
                //checkForAndAskForPermissions()
                startWebView(roomName.text.toString())
            } else {
                roomName.error = "Enter room name!"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }

    private fun startWebView(roomName: String) {
        val intent = Intent(this, WebViewActivity::class.java).apply {
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
                BroadcastEvent.Type.AUDIO_MUTED_CHANGED -> println("Mute selected")
                BroadcastEvent.Type.CONFERENCE_JOINED -> println("Conference Joined with url ${event.data["url"]}")
                BroadcastEvent.Type.PARTICIPANT_JOINED -> println("Participant joined ${event.data["name"]}")
                else -> return
            }
        }
    }
}