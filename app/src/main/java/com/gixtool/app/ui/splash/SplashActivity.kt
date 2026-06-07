package com.gixtool.app.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gixtool.app.MainActivity
import com.gixtool.app.R
import com.gixtool.app.renderer.GIXRenderer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val glView = GLSurfaceView(this).apply {
            setEGLContextClientVersion(2)
            setRenderer(GIXRenderer(this@SplashActivity))
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }

        findViewById<android.widget.FrameLayout>(R.id.glContainer).addView(glView)

        lifecycleScope.launch {
            delay(3000)
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}
