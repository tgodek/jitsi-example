package com.mangata.jitsiexample.featureEmbedded

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.doOnPreDraw
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.facebook.react.modules.core.PermissionListener
import com.mangata.jitsiexample.databinding.ActivityEmbeddedBinding
import com.mangata.jitsiexample.util.navigationBarHeight
import org.jitsi.meet.sdk.*
import org.koin.androidx.viewmodel.ext.android.stateViewModel

@RequiresApi(Build.VERSION_CODES.R)
class EmbeddedActivity : AppCompatActivity(), JitsiMeetActivityInterface {

    private val viewModel: EmbeddedViewModel by stateViewModel(state = { Bundle(intent.extras) })
    private lateinit var binding: ActivityEmbeddedBinding
    private var jitsiMeetView: JitsiMeetView? = null
    private var portraitScreenWidth: Int? = null
    private var portraitScreenHeight: Int? = null
    private var portraitFrameHeight: Int? = null

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            onBroadcastReceived(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityEmbeddedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val videoView = binding.frameLayout
        jitsiMeetView = JitsiMeetView(this)

        registerForBroadcastMessages()

        /**
         * If the calculated height of the Frame Layout is less than 900dp
         * we need to force the layout height to at least 900dp else the video call options won't show
         */
        setupVideoFrame(videoView)

        if (!viewModel.conferenceJoined) {
            jitsiMeetView?.join(viewModel.onConferenceJoinConfig())
        }
    }

    private fun setupVideoFrame(videoView: FrameLayout) {
        videoView.doOnPreDraw {
            val rootWidth = resources.displayMetrics.widthPixels
            val rootHeight = resources.displayMetrics.heightPixels
            val orientation = resources.configuration.orientation

            var videoHeight = it.height
            var videoWidth = it.width

            if (videoHeight < 900 && orientation == Configuration.ORIENTATION_PORTRAIT) {
                videoHeight = 900

                portraitScreenWidth = rootWidth
                portraitScreenHeight = rootHeight + navigationBarHeight
                portraitFrameHeight = videoHeight
            }

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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val orientation = newConfig.orientation

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.frameLayout.layoutParams.width = portraitScreenHeight!!
            binding.frameLayout.layoutParams.height = portraitScreenWidth!!

            jitsiMeetView?.layoutParams?.width = FrameLayout.LayoutParams.MATCH_PARENT
            jitsiMeetView?.layoutParams?.height = FrameLayout.LayoutParams.MATCH_PARENT
        }

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.frameLayout.layoutParams.width = portraitScreenWidth!!
            binding.frameLayout.layoutParams?.height = portraitFrameHeight!!

            jitsiMeetView?.layoutParams?.width = FrameLayout.LayoutParams.MATCH_PARENT
            jitsiMeetView?.layoutParams?.height = FrameLayout.LayoutParams.MATCH_PARENT
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
}