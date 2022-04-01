package com.mangata.jitsiexample.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mangata.jitsiexample.R
import com.mangata.jitsiexample.databinding.FragmentHomeBinding
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import java.net.URL

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isCameraPermissionGranted = false
    private var isMicrophonePermissionGranted = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentHomeBinding.bind(view)

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                isCameraPermissionGranted =
                    permissions[Manifest.permission.CAMERA] ?: isCameraPermissionGranted
                isMicrophonePermissionGranted =
                    permissions[Manifest.permission.RECORD_AUDIO] ?: isMicrophonePermissionGranted
            }

        checkForAndAskForPermissions()

        binding.apply {
            nativeJoinButton.setOnClickListener {
                if (validInput(roomNameEditTxt))
                    startNativeView(roomNameEditTxt.text.toString())
            }
            webViewJoinButton.setOnClickListener {
                if (validInput(roomNameEditTxt))
                    startWebView(roomNameEditTxt.text.toString())
            }
            embededJoinButton.setOnClickListener {
                if (validInput(roomNameEditTxt))
                    startEmbeddedView(roomNameEditTxt.text.toString())
            }
        }
    }

    private fun startWebView(roomName: String) {
        val action = HomeFragmentDirections.actionHomeFragmentToWebViewFragment(roomName)
        findNavController().navigate(action)
    }

    private fun startEmbeddedView(roomName: String) {
        val action = HomeFragmentDirections.actionHomeFragmentToMeetingActivity(roomName)
        findNavController().navigate(action)
    }

    private fun startNativeView(roomName: String) {
        val options = JitsiMeetConferenceOptions.Builder()
            .setServerURL(URL("https://meet.jit.si"))
            .setRoom(roomName)
            .build()

        JitsiMeetActivity.launch(activity, options)
    }

    private fun validInput(roomName: EditText): Boolean {
        if (roomName.text.isEmpty()) {
            roomName.error = "Enter room name!"
            return false
        }
        if (!permissionsGranted()) {
            checkForAndAskForPermissions()
            return false
        }
        return true
    }

    private fun permissionsGranted(): Boolean =
        isMicrophonePermissionGranted && isCameraPermissionGranted

    private fun checkForAndAskForPermissions() {
        val permissionRequest: MutableList<String> = ArrayList()

        isCameraPermissionGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        isMicrophonePermissionGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!isCameraPermissionGranted) {
            permissionRequest.add(Manifest.permission.CAMERA)
        }
        if (!isMicrophonePermissionGranted) {
            permissionRequest.add(Manifest.permission.RECORD_AUDIO)
        }
        if (permissionRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }
    }
}