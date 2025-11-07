package com.example.ma_final_project

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FakeVoiceCallActivity : AppCompatActivity() {

    private lateinit var tvCallerName: TextView
    private lateinit var tvTimer: TextView
    private lateinit var btnEnd: Button
    private lateinit var btnSpeaker: Button

    private var player: MediaPlayer? = null
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fake_voice_call)

        tvCallerName = findViewById(R.id.tvCallerName)
        tvTimer = findViewById(R.id.tvTimer)
        btnEnd = findViewById(R.id.btnEnd)
        btnSpeaker = findViewById(R.id.btnSpeaker)

        val name = intent.getStringExtra("caller_name")
        tvCallerName.text = if (name.isNullOrBlank()) "Jordan (Voice)" else "$name (Voice)"

        setupSpeaker(true)
        startTimer()
        playSingleClip()

        btnEnd.setOnClickListener { finish() }
        btnSpeaker.setOnClickListener {
            val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            setupSpeaker(!am.isSpeakerphoneOn)
        }
    }

    private fun playSingleClip() {
        // your one clip here:
        val resId = R.raw.call_message

        stopVoice()
        player = MediaPlayer.create(this, resId)
        player?.setOnCompletionListener {
            // when message finishes, end the fake call
            finish()
        }
        player?.start()
    }

    private fun startTimer() {
        timer = object : CountDownTimer(60 * 60 * 1000, 1000) {
            var elapsed = 0
            override fun onTick(ms: Long) {
                elapsed++
                val m = elapsed / 60
                val s = elapsed % 60
                tvTimer.text = String.format("%02d:%02d", m, s)
            }
            override fun onFinish() {}
        }.start()
    }

    private fun setupSpeaker(on: Boolean) {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.mode = AudioManager.MODE_NORMAL
        am.isSpeakerphoneOn = on
        btnSpeaker.text = if (on) "Speaker On" else "Speaker Off"
    }

    private fun stopVoice() {
        player?.let {
            try { if (it.isPlaying) it.stop() } catch (_: Exception) {}
            it.release()
        }
        player = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVoice()
        timer?.cancel()
    }
}
