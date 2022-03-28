package com.mangata.jitsiexample.feature_embedded

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.doOnPreDraw
import com.facebook.react.modules.core.PermissionListener
import com.mangata.jitsiexample.databinding.ActivityEmbeddedBinding
import com.mangata.jitsiexample.util.navigationBarHeight
import org.jitsi.meet.sdk.JitsiMeetActivityDelegate
import org.jitsi.meet.sdk.JitsiMeetActivityInterface
import org.jitsi.meet.sdk.JitsiMeetView
import org.koin.androidx.viewmodel.ext.android.stateViewModel

@RequiresApi(Build.VERSION_CODES.R)
class EmbeddedActivity : AppCompatActivity(), JitsiMeetActivityInterface {

    private val viewModel: EmbeddedViewModel by stateViewModel(state = { Bundle(intent.extras) })
    private lateinit var binding: ActivityEmbeddedBinding
    private var jitsiMeetView: JitsiMeetView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityEmbeddedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val videoView = binding.customFrameLayout
        jitsiMeetView = JitsiMeetView(this)

        // If the calculated height of the Frame Layout is less than 900dp
        // we need to force the layout height to at least 900dp else the video call options won't show
        setupVideoFrame(videoView)

        if (!viewModel.conferenceJoined)
            jitsiMeetView?.join(viewModel.onConferenceJoinConfig())
    }


    private fun setupVideoFrame(videoView: FrameLayout) {
        videoView.doOnPreDraw {
            val rootWidth = resources.displayMetrics.widthPixels
            val rootHeight = resources.displayMetrics.heightPixels
            var videoHeight = it.height
            var videoWidth = it.width
            val orientation = resources.configuration.orientation

            if (videoHeight < 900 && orientation == Configuration.ORIENTATION_PORTRAIT)
                videoHeight = 900

            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                videoWidth = rootWidth + navigationBarHeight
                videoHeight = rootHeight
            }

            it.apply {
                layoutParams.height = videoHeight
                layoutParams.width = videoWidth
            }
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