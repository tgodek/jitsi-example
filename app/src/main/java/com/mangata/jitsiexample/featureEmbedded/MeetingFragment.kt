package com.mangata.jitsiexample.featureEmbedded

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mangata.jitsiexample.R
import com.mangata.jitsiexample.databinding.FragmentMeetingBinding
import com.mangata.jitsiexample.util.navigationBarHeight
import org.devio.rn.splashscreen.SplashScreen.hide
import org.jitsi.meet.sdk.BroadcastEvent
import org.jitsi.meet.sdk.BroadcastIntentHelper
import org.jitsi.meet.sdk.JitsiMeetView
import org.koin.androidx.viewmodel.ext.android.viewModel

@RequiresApi(Build.VERSION_CODES.R)
class MeetingFragment : Fragment(R.layout.fragment_meeting) {

    private var _binding: FragmentMeetingBinding? = null
    private val binding get() = _binding!!
    private val args: MeetingFragmentArgs by navArgs()
    private val viewModel: MeetingViewModel by viewModel()
    private var jitsiMeetView: JitsiMeetView? = null
    private var portraitScreenWidth: Int? = null
    private var portraitScreenHeight: Int? = null
    private var portraitFrameHeight: Int? = null
    private lateinit var toolbar: Toolbar
    private lateinit var bottomNavBar: BottomNavigationView

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            onBroadcastReceived(intent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMeetingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val videoView = binding.frameLayout
        jitsiMeetView = JitsiMeetView(requireActivity())
        toolbar = requireActivity().findViewById(R.id.toolbar)
        bottomNavBar = requireActivity().findViewById(R.id.bottom_nav)

        toolbar.setNavigationOnClickListener {
           onBackPressed()
        }

        val onBackPressed = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = onBackPressed()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressed)

        if (!viewModel.conferenceJoined)
            jitsiMeetView?.join(viewModel.onConferenceJoinConfig(args.roomName))

        registerForBroadcastMessages()

        /**
         * If the calculated height of the Frame Layout is less than 900dp
         * we need to force the layout height to at least 900dp else the video call options won't show
         */
        setupVideoFrame(videoView)
    }

    private fun onBackPressed() {
        when {
            !viewModel.conferenceTerminated -> showAlertDialog()
            else ->  findNavController().navigate(MeetingFragmentDirections.actionMeetingFragmentToHomeFragment())
        }
    }

    private fun showAlertDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Leave Meeting")
            .setMessage("Are you sure you want to leave this meeting?")
            .setPositiveButton(R.string.alert_dialog_positive) { dialog, _ ->
                hangUp()
                dialog.dismiss()
                findNavController().navigate(MeetingFragmentDirections.actionMeetingFragmentToHomeFragment())
            }
            .setNegativeButton(R.string.alert_dialog_negative, null)
            .show()
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
                portraitScreenHeight = rootHeight + requireActivity().navigationBarHeight
                portraitFrameHeight = videoHeight
            }

            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                videoWidth = rootWidth + requireActivity().navigationBarHeight
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
            toolbar.visibility = View.GONE
            binding.frameLayout.layoutParams.width = portraitScreenHeight!!
            binding.frameLayout.layoutParams.height = portraitScreenWidth!!

            jitsiMeetView?.layoutParams?.width = FrameLayout.LayoutParams.MATCH_PARENT
            jitsiMeetView?.layoutParams?.height = FrameLayout.LayoutParams.MATCH_PARENT
        }

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            toolbar.visibility = View.VISIBLE
            binding.frameLayout.layoutParams.width = portraitScreenWidth!!
            binding.frameLayout.layoutParams?.height = portraitFrameHeight!!

            jitsiMeetView?.layoutParams?.width = FrameLayout.LayoutParams.MATCH_PARENT
            jitsiMeetView?.layoutParams?.height = FrameLayout.LayoutParams.MATCH_PARENT
        }
    }

    private fun hangUp() {
        val hangupBroadcastIntent: Intent = BroadcastIntentHelper.buildHangUpIntent()
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(hangupBroadcastIntent)
    }

    private fun registerForBroadcastMessages() {
        val intentFilter = IntentFilter()

        for (type in BroadcastEvent.Type.values()) {
            intentFilter.addAction(type.action)
        }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiver, intentFilter)
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
                BroadcastEvent.Type.CONFERENCE_WILL_JOIN -> println("JitsiEvent3")
                BroadcastEvent.Type.AUDIO_MUTED_CHANGED -> println("JitsiEvent4")
                BroadcastEvent.Type.PARTICIPANT_JOINED -> println("JitsiEvent5")
                BroadcastEvent.Type.PARTICIPANT_LEFT -> println("JitsiEvent6")
                BroadcastEvent.Type.ENDPOINT_TEXT_MESSAGE_RECEIVED -> println("JitsiEvent7")
                BroadcastEvent.Type.SCREEN_SHARE_TOGGLED -> println("JitsiEvent8")
                BroadcastEvent.Type.PARTICIPANTS_INFO_RETRIEVED -> println("JitsiEvent9")
                BroadcastEvent.Type.CHAT_MESSAGE_RECEIVED -> println("JitsiEvent10")
                BroadcastEvent.Type.CHAT_TOGGLED -> println("JitsiEvent11")
                BroadcastEvent.Type.VIDEO_MUTED_CHANGED -> println("JitsiEvent12")
                BroadcastEvent.Type.READY_TO_CLOSE -> println("JitsiEvent13")
                else -> return
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        jitsiMeetView?.dispose()
        jitsiMeetView = null
        _binding = null
    }
}