package com.mangata.jitsiexample.util

import android.app.Activity
import android.content.res.Configuration
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.mangata.jitsiexample.BuildConfig
import com.mangata.jitsiexample.R
import org.jitsi.meet.sdk.JitsiMeetView

fun Activity.getJitsiView(): JitsiMeetView? {
    when (BuildConfig.isVideoFeatureEnabled) {
        true -> {
            return JitsiMeetView(this)
        }
        false -> {
            return null
        }
    }
}

fun ImageView.configureFullScreenIcon() {
    this.setColorFilter(resources.getColor(R.color.white, context.theme))
    val orientation = resources.configuration.orientation

    val params = FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.WRAP_CONTENT,
        FrameLayout.LayoutParams.WRAP_CONTENT
    ).apply {
        gravity = Gravity.TOP or Gravity.END
        rightMargin = 50
        topMargin = 50
    }

    this.apply {
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setImageResource(R.drawable.ic_fullscreen)
        } else {
            setImageResource(R.drawable.ic_close_fullscreen)
        }
        layoutParams = params
    }
}