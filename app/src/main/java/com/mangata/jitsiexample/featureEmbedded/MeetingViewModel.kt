package com.mangata.jitsiexample.featureEmbedded

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import java.net.URL

class MeetingViewModel : ViewModel() {

    private val _videoConferenceState = MutableStateFlow(false)
    val videoConferenceState: StateFlow<Boolean> = _videoConferenceState

    fun onConferenceJoinConfig(roomName: String): JitsiMeetConferenceOptions {
        return JitsiMeetConferenceOptions
            .Builder()
            .setServerURL(URL("https://meet.jit.si"))
            .setRoom(roomName)
            .setFeatureFlag("invite.enabled", false)
            .build()
    }

    fun onEvent(event: MeetingActivityEvents) {
        when (event) {
            MeetingActivityEvents.ConferenceJoined -> {
                _videoConferenceState.value = true
            }
            MeetingActivityEvents.ConferenceTerminated -> {
                _videoConferenceState.value = false
            }
        }
    }
}

sealed class MeetingActivityEvents {
    object ConferenceJoined : MeetingActivityEvents()
    object ConferenceTerminated : MeetingActivityEvents()
}