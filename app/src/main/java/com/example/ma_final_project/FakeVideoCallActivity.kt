package com.example.ma_final_project

import android.Manifest
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import java.util.concurrent.Executors
import android.view.View
import android.view.MotionEvent
import android.util.Size



import kotlin.random.Random

class FakeVideoCallActivity : AppCompatActivity() {

    private lateinit var playerView: StyledPlayerView
    private lateinit var selfPreview: PreviewView
    private lateinit var tvCallerName: TextView
    private lateinit var tvTimer: TextView
    private lateinit var btnEnd: Button
    private lateinit var btnSpeaker: Button

    private var player: ExoPlayer? = null
    private var timer: CountDownTimer? = null
    private val cameraExecutor by lazy { Executors.newSingleThreadExecutor() }

    // ask only CAMERA; we’re not recording you, just showing preview
    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startSelfPreview()
        // if denied, continue call without self view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_fake_video_call)

        playerView = findViewById(R.id.remotePlayerView)
        selfPreview = findViewById(R.id.selfPreview)
        tvCallerName = findViewById(R.id.tvCallerName)
        tvTimer = findViewById(R.id.tvTimer)
        btnEnd = findViewById(R.id.btnEnd)
        btnSpeaker = findViewById(R.id.btnSpeaker)

        enableDrag(selfPreview)


        tvCallerName.text = randomCallerName()
        setupSpeaker(true)             // start on speaker
        setupRemoteVideo()             // play the “other person”
        startTimer()                   // 00:00 → …
        requestCamera()
        wireClicks()
    }

    private fun requestCamera() {
        permLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun setupRemoteVideo() {
        playerView.useController = false
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        playerView.setShowBuffering(StyledPlayerView.SHOW_BUFFERING_WHEN_PLAYING)

        val uri = RawResourceDataSource.buildRawResourceUri(R.raw.caller_a)

        player = ExoPlayer.Builder(this).build().also { p ->
            playerView.player = p
            p.setMediaItem(MediaItem.fromUri(uri))
            p.repeatMode = Player.REPEAT_MODE_ONE
            p.prepare()
            p.playWhenReady = true
        }
    }

    private fun startSelfPreview() {
        selfPreview.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        selfPreview.scaleType = PreviewView.ScaleType.FILL_CENTER
        selfPreview.bringToFront()

        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            val provider = providerFuture.get()
            val preview = Preview.Builder()
                .setTargetResolution(Size(720, 1080))
                .setTargetRotation(selfPreview.display.rotation)
                .build().also {
                    it.setSurfaceProvider(selfPreview.surfaceProvider)
                }
            try {
                provider.unbindAll()
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, preview)
                selfPreview.alpha = 1f
            } catch (e: Exception) {
                selfPreview.alpha = 0.8f
                tvCallerName.text = "Front camera unavailable"
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun wireClicks() {
        btnEnd.setOnClickListener { finish() }
        btnSpeaker.setOnClickListener {
            val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            setupSpeaker(!am.isSpeakerphoneOn)
        }
    }

    private fun setupSpeaker(on: Boolean) {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.mode = AudioManager.MODE_NORMAL
        am.isSpeakerphoneOn = on
        btnSpeaker.text = if (on) "Speaker On" else "Speaker Off"
    }

    private fun startTimer() {
        // simple up-counter using CountDownTimer trick (1 hour cap)
        timer = object : CountDownTimer(60 * 60 * 1000, 1000) {
            var elapsed = 0
            override fun onTick(ms: Long) {
                elapsed += 1
                val m = elapsed / 60
                val s = elapsed % 60
                tvTimer.text = String.format("%02d:%02d", m, s)
            }
            override fun onFinish() {}
        }.start()
    }

    private fun randomCallerName(): String {
        val names = listOf("Ava", "Jordan", "Sam", "Taylor", "Alex")
        return "${names.random()} (Video)"
    }

    // helper to convert dp → px
    private fun dp(v: Int): Float = v * resources.displayMetrics.density

    private fun enableDrag(view: View) {
        var dX = 0f
        var dY = 0f
        view.isClickable = true

        view.setOnTouchListener { v:View, e: MotionEvent ->
            val parent = v.parent as View
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - e.rawX
                    dY = v.y - e.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    var newX = e.rawX + dX
                    var newY = e.rawY + dY
                    // keep inside parent bounds
                    val maxX = parent.width - v.width
                    val maxY = parent.height - v.height
                    newX = newX.coerceIn(0f, maxX.toFloat())
                    newY = newY.coerceIn(0f, maxY.toFloat())
                    v.x = newX
                    v.y = newY
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // snap to nearest corner
                    val midX = parent.width / 2f
                    val midY = parent.height / 2f
                    val targetX = if (v.x + v.width / 2f < midX) dp(16) else parent.width - v.width - dp(16)
                    val targetY = if (v.y + v.height / 2f < midY) dp(16) else parent.height - v.height - dp(16)
                    v.animate().x(targetX).y(targetY).setDuration(150).start()
                    true
                }
                else -> false
            }
        }
    }


    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release(); player = null
        timer?.cancel(); timer = null
        cameraExecutor.shutdown()
    }
}
