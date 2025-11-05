package com.example.ma_final_project

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class IncomingCallActivity : AppCompatActivity() {

    private lateinit var tvCallerName: TextView
    private lateinit var btnAnswer: Button
    private lateinit var btnDecline: Button

    private var ringPlayer: MediaPlayer? = null
    private var connected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_call)

        tvCallerName = findViewById(R.id.tvCallerName)
        btnAnswer = findViewById(R.id.btnAnswer)
        btnDecline = findViewById(R.id.btnDecline)

        tvCallerName.text = pickCallerName()

        startRinging()

        btnAnswer.setOnClickListener { connectNow() }
        btnDecline.setOnClickListener { finish() }
    }

    private fun pickCallerName(): String {
        val names = listOf("Ava", "Jordan", "Sam", "Taylor", "Alex")
        return names.random()
    }

    private fun startRinging() {
        // Route audio to speaker so it's audible
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.mode = AudioManager.MODE_NORMAL
        am.isSpeakerphoneOn = true


        // Loop ringtone until user answers/declines
        ringPlayer = MediaPlayer.create(this, R.raw.ringtone_incoming)?.apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            isLooping = true
            start()
        }
    }

    private fun connectNow() {
        if (connected) return
        connected = true
        stopRinging()

        val i = Intent(this, FakeVideoCallActivity::class.java)
        i.putExtra("caller_name", tvCallerName.text.toString())
        startActivity(i)
        finish()
    }

    private fun stopRinging() {
        ringPlayer?.let {
            try { if (it.isPlaying) it.stop() } catch (_: Exception) {}
            it.release()
        }
        ringPlayer = null
    }

    override fun onStop() {
        super.onStop()
        // If user backgrounds the screen, pause ring to be polite
        ringPlayer?.pause()
    }

    override fun onStart() {
        super.onStart()
        // Resume ring if still incoming
        ringPlayer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRinging()
    }
}
