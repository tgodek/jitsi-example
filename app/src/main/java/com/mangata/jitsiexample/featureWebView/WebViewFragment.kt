package com.mangata.jitsiexample.featureWebView

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.facebook.react.bridge.UiThreadUtil.runOnUiThread
import com.mangata.jitsiexample.R
import com.mangata.jitsiexample.databinding.FragmentWebViewBinding
import com.mangata.jitsiexample.util.Constants
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import java.net.URL

class WebViewFragment : Fragment() {

    private var _binding: FragmentWebViewBinding? = null
    private val binding get() = _binding!!
    private val args: WebViewFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWebViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val webView = binding.liveMeetWebView
        val roomName = args.roomName

        val options = JitsiMeetConferenceOptions.Builder()
            .setServerURL(URL("https://meet.jit.si"))
            .setRoom("$roomName#config.disableDeepLinking=true")
            .build()

        val webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                runOnUiThread {
                    request?.grant(request.resources)
                }
            }
        }

        val webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return findNavController().popBackStack()
            }
        }

        webView.apply {
            settings.javaScriptEnabled = true
            // Use WideViewport if there is no viewport defined
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true

            this.webChromeClient = webChromeClient
            this.webViewClient = webViewClient
            this.loadUrl("${options.serverURL}/${options.room}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.liveMeetWebView.evaluateJavascript("if(window.localStream){window.localStream.stop();}", null)
        _binding = null
    }
}