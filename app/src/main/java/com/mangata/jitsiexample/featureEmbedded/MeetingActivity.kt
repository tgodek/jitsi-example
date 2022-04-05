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
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.navArgs
import com.facebook.react.modules.core.PermissionListener
import com.mangata.jitsiexample.R
import com.mangata.jitsiexample.databinding.ActivityMeetingBinding
import com.mangata.jitsiexample.util.configureFullScreenIcon
import com.mangata.jitsiexample.util.getJitsiView
import com.mangata.jitsiexample.util.navigationBarHeight
import kotlinx.coroutines.launch
import org.jitsi.meet.sdk.*
import org.koin.androidx.viewmodel.ext.android.viewModel

@RequiresApi(Build.VERSION_CODES.R)
class MeetingActivity : AppCompatActivity(), JitsiMeetActivityInterface {

    private lateinit var binding: ActivityMeetingBinding
    private val viewModel: MeetingViewModel by viewModel()
    private val args: MeetingActivityArgs by navArgs()

    private var jitsiMeetView: JitsiMeetView? = null
    private lateinit var fullScreenIcon : ImageView
    private var portraitFrameHeight: Int = 0

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            onBroadcastReceived(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMeetingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        jitsiMeetView = getJitsiView()
        fullScreenIcon = ImageView(this)
        fullScreenIcon.visibility = View.GONE

        setSupportActionBar(binding.toolbar)
        val frameLayout = binding.frameLayout

        registerForBroadcastMessages()

        binding.toolbar.setNavigationOnClickListener {
            handleOnBackPressed()
        }

        jitsiMeetView?.join(viewModel.onConferenceJoinConfig(args.roomName))

        /**
         * If the calculated height of the Frame Layout is less than 900dp
         * we need to force the layout height to at least 900dp else the video call options won't show
         */
        setupVideoFrame(frameLayout)


        fullScreenIcon.setOnClickListener {

            val orientation = resources.configuration.orientation

            when(orientation) {
                Configuration.ORIENTATION_PORTRAIT -> {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
                Configuration.ORIENTATION_LANDSCAPE -> {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.videoConferenceState.collect { joined ->
                    if (joined && jitsiMeetView != null) {
                        fullScreenIcon.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        handleOnBackPressed()
    }

    private fun handleOnBackPressed() {
        when {
            !viewModel.videoConferenceState.value -> finish()
            else -> showAlertDialog()
        }
    }

    private fun showAlertDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.alert_title)
            .setMessage(R.string.alert_message)
            .setPositiveButton(R.string.alert_dialog_positive) { dialog, _ ->
                hangUp()
                dialog.dismiss()
                finish()
            }
            .setNegativeButton(R.string.alert_dialog_negative, null)
            .show()
    }

    private fun setupVideoFrame(frameLayout: FrameLayout) {
        frameLayout.doOnPreDraw {
            val rootWidth = resources.displayMetrics.widthPixels
            val rootHeight = resources.displayMetrics.heightPixels
            val orientation = resources.configuration.orientation

            fullScreenIcon.configureFullScreenIcon()

            var videoHeight = it.height
            var videoWidth = it.width

            if (videoHeight < 900 && orientation == Configuration.ORIENTATION_PORTRAIT) {
                videoHeight = 900
                portraitFrameHeight = videoHeight
            }

            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                binding.toolbar.visibility = View.GONE
                videoWidth = rootWidth + navigationBarHeight
                videoHeight = rootHeight
            }

            it.apply {
                layoutParams.height = videoHeight
                layoutParams.width = videoWidth
            }

            if (jitsiMeetView != null) {
                frameLayout.addView(jitsiMeetView, videoWidth, videoHeight)
                frameLayout.addView(fullScreenIcon, 1)
            } else {
                frameLayout.setBackgroundColor(resources.getColor(R.color.black, theme))
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val orientation = newConfig.orientation

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.toolbar.visibility = View.GONE
            fullScreenIcon.setImageResource(R.drawable.ic_close_fullscreen)
            binding.frameLayout.layoutParams.apply {
                width = FrameLayout.LayoutParams.MATCH_PARENT
                height = FrameLayout.LayoutParams.MATCH_PARENT
            }

            jitsiMeetView?.layoutParams?.apply {
                width = FrameLayout.LayoutParams.MATCH_PARENT
                height = FrameLayout.LayoutParams.MATCH_PARENT
            }
        }

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.toolbar.visibility = View.VISIBLE
            fullScreenIcon.setImageResource(R.drawable.ic_fullscreen)
            binding.frameLayout.layoutParams.apply {
                width = FrameLayout.LayoutParams.MATCH_PARENT
                height = if (portraitFrameHeight >= 900) portraitFrameHeight else 900
            }

            jitsiMeetView?.layoutParams?.apply {
                width = FrameLayout.LayoutParams.MATCH_PARENT
                height = FrameLayout.LayoutParams.MATCH_PARENT
            }
        }
    }

    private fun hangUp() {
        val hangupBroadcastIntent: Intent = BroadcastIntentHelper.buildHangUpIntent()
        LocalBroadcastManager.getInstance(this).sendBroadcast(hangupBroadcastIntent)
    }

    private fun registerForBroadcastMessages() {
        val intentFilter = IntentFilter()

        for (type in BroadcastEvent.Type.values()) {
            intentFilter.addAction(type.action)
        }
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun onBroadcastReceived(intent: Intent?) {
        intent?.let {
            val event = BroadcastEvent(it)
            when (event.type) {
                BroadcastEvent.Type.CONFERENCE_JOINED -> {
                    viewModel.onEvent(EmbeddedActivityEvents.ConferenceJoined)
                }
                BroadcastEvent.Type.CONFERENCE_TERMINATED -> {
                    viewModel.onEvent(EmbeddedActivityEvents.ConferenceTerminated)
                }
                else -> return
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        JitsiMeetActivityDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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