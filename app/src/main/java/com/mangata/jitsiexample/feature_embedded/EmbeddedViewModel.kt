package com.mangata.jitsiexample.feature_embedded

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.mangata.jitsiexample.util.Constants
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import java.net.URL

class EmbeddedViewModel(
    handle: SavedStateHandle
) : ViewModel() {

    private val roomName = handle.get<String>(Constants.ROOM_NAME)

    var conferenceJoined = false
        private set

    fun onConferenceJoinConfig(): JitsiMeetConferenceOptions {
        println("roomName: $roomName")
        conferenceJoined = true
        return JitsiMeetConferenceOptions
            .Builder()
            .setServerURL(URL("https://meet.jit.si"))
            .setRoom(roomName)
            .build()
    }
}