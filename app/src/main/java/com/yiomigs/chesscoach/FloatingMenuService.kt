package com.yiomigs.chesscoach

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class FloatingMenuService : Service() {
    private lateinit var wm: WindowManager
    private var panel: LinearLayout? = null
    private var player: MediaPlayer? = null
    private var playing = false
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_AUDIO) intent.getStringExtra(EXTRA_URI)?.let { loadAudio(Uri.parse(it)) }
        if (panel == null && Settings.canDrawOverlays(this)) showPanel()
        return START_STICKY
    }
    private fun showPanel() {
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 14, 18, 14)
            setBackgroundColor(Color.rgb(20, 34, 47))
        }
        box.addView(TextView(this).apply {
            text = "♟ Chess Coach"; setTextColor(Color.WHITE); textSize = 16f; setPadding(4,4,4,10)
        })
        box.addView(Button(this).apply { text = "Музыка: ▶ / пауза"; setOnClickListener { toggleMusic() } })
        box.addView(Button(this).apply { text = "Закрыть меню"; setOnClickListener { stopSelf() } })
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.END; x = 18; y = 140 }
        panel = box
        wm.addView(box, params)
    }
    private fun loadAudio(uri: Uri) {
        try {
            player?.release()
            player = MediaPlayer().apply {
                setDataSource(this@FloatingMenuService, uri)
                isLooping = true
                setOnPreparedListener { it.start(); playing = true }
                prepareAsync()
            }
        } catch (_: Exception) { playing = false }
    }
    private fun toggleMusic() {
        val p = player ?: return
        if (playing) { p.pause(); playing = false } else { p.start(); playing = true }
    }
    override fun onDestroy() {
        panel?.let { try { wm.removeView(it) } catch (_: Exception) {} }
        panel = null
        player?.release(); player = null
        super.onDestroy()
    }
    companion object {
        const val ACTION_AUDIO = "com.yiomigs.chesscoach.SET_AUDIO"
        const val EXTRA_URI = "audio_uri"
    }
}