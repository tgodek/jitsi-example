package com.mangata.jitsiexample.util

import android.app.Activity
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import com.mangata.jitsiexample.BuildConfig
import com.mangata.jitsiexample.R
import org.jitsi.meet.sdk.JitsiMeetView

fun Activity.getJitsiView(): JitsiMeetView? {

    return when (BuildConfig.isVideoFeatureEnabled) {
        true -> {
            JitsiMeetView(this)
        }
        false -> {
            return null
        }
    }
}