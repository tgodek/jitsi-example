package com.mangata.jitsiexample.featureEmbedded

import androidx.lifecycle.ViewModel
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import java.net.URL

class EmbeddedViewModel : ViewModel() {

    var conferenceJoined = false
        private set

    var conferenceTerminated = false
        private set

    fun onConferenceJoinConfig(roomName: String): JitsiMeetConferenceOptions {
        return JitsiMeetConferenceOptions
            .Builder()
            .setServerURL(URL("https://meet.jit.si"))
            .setRoom(roomName)
            .setFeatureFlag("call-integration.enabled", false)
            .build()
    }

    fun onEvent(event: EmbeddedActivityEvents) {
        when (event) {
            EmbeddedActivityEvents.ConferenceJoined -> {
                println("Conference Joined")
                conferenceJoined = true
            }
            EmbeddedActivityEvents.ConferenceTerminated -> {
                println("Conference Terminated")
                conferenceTerminated = true
            }
        }
    }
}

sealed class EmbeddedActivityEvents {
    object ConferenceJoined : EmbeddedActivityEvents()
    object ConferenceTerminated : EmbeddedActivityEvents()
}