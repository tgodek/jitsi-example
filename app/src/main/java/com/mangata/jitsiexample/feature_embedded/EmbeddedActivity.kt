package com.mangata.jitsiexample.feature_embedded

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.doOnPreDraw
import com.facebook.react.modules.core.PermissionListener
import com.mangata.jitsiexample.databinding.ActivityEmbeddedBinding
import org.jitsi.meet.sdk.JitsiMeetActivityDelegate
import org.jitsi.meet.sdk.JitsiMeetActivityInterface
import org.jitsi.meet.sdk.JitsiMeetView
import org.koin.androidx.viewmodel.ext.android.stateViewModel


class EmbeddedActivity : AppCompatActivity(), JitsiMeetActivityInterface {

    private val viewModel: EmbeddedViewModel by stateViewModel(state = { Bundle(intent.extras) })
    private lateinit var binding: ActivityEmbeddedBinding
    private var jitsiMeetView: JitsiMeetView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmbeddedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val videoView = binding.customFrameLayout
        jitsiMeetView = JitsiMeetView(this)

        jitsiMeetView?.join(viewModel.setupConference())

        // If the calculated height of the Frame Layout is less than 900dp
        // we need to force the layout to 900dp else the video call options won't show
        videoView.doOnPreDraw {
            var videoHeight = videoView.height
            val videoWidth = videoView.width
            if (videoHeight < 900)
                videoHeight = 900

            videoView.layoutParams.height = videoHeight
            videoView.layoutParams.width = videoWidth
            videoView.addView(jitsiMeetView, videoWidth, videoHeight)
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        JitsiMeetActivityDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun requestPermissions(p0: Array<out String>?, p1: Int, p2: PermissionListener?) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
            p1
        )
    }

    override fun onResume() {
        super.onResume()
        JitsiMeetActivityDelegate.onHostResume(this)
    }

    override fun onStop() {
        super.onStop()
        JitsiMeetActivityDelegate.onHostPause(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        JitsiMeetActivityDelegate.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        JitsiMeetActivityDelegate.onActivityResult(this, requestCode, resultCode, data)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        JitsiMeetActivityDelegate.onNewIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        jitsiMeetView?.dispose()
        jitsiMeetView = null
        JitsiMeetActivityDelegate.onHostDestroy(this)
    }

}