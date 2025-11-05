package com.example.ma_final_project

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class IncomingVoiceActivity : AppCompatActivity() {

    private lateinit var tvCallerName: TextView
    private lateinit var btnAnswer: Button
    private lateinit var btnDecline: Button

    private var ringPlayer: MediaPlayer? = null
    private var connected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_voice)

        tvCallerName = findViewById(R.id.tvCallerName)
        btnAnswer = findViewById(R.id.btnAnswer)
        btnDecline = findViewById(R.id.btnDecline)

        tvCallerName.text = pickCallerName()

        startRinging()

        btnAnswer.setOnClickListener { connectNow() }
        btnDecline.setOnClickListener { finish() }
    }

    private fun pickCallerName(): String {
        // change these names if you want
        val names = listOf("Jordan", "Ava", "Mom", "Uncle", "Boss")
        return names.random()
    }

    private fun startRinging() {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.mode = AudioManager.MODE_NORMAL
        am.isSpeakerphoneOn = true

        vibratePulse()

        stopRinging()
        ringPlayer = MediaPlayer.create(this, R.raw.ringtone_incoming)
        ringPlayer?.isLooping = true
        ringPlayer?.start()
    }

    private fun stopRinging() {
        ringPlayer?.let {
            try { if (it.isPlaying) it.stop() } catch (_: Exception) {}
            it.release()
        }
        ringPlayer = null
    }

    private fun vibratePulse() {
        val vib = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vib.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun connectNow() {
        if (connected) return
        connected = true
        stopRinging()

        val intent = Intent(this, FakeVoiceCallActivity::class.java)
        intent.putExtra("caller_name", tvCallerName.text.toString())
        startActivity(intent)
        finish()
    }

    override fun onStop() {
        super.onStop()
        ringPlayer?.pause()
    }

    override fun onStart() {
        super.onStart()
        ringPlayer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRinging()
    }
}
