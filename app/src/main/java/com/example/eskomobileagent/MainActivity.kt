package com.example.eskomobileagent

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.eskomobileagent.tracking.EskoMobileAgent

class MainActivity : AppCompatActivity() {
    private val userId = "U1"
    private val product = "MobileApp"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        EskoMobileAgent.startSession(this, userId, product)
    }

    override fun onPause() {
        super.onPause()

        // Before ending session, check if user was idle
        EskoMobileAgent.maybeSendIdleEvent(userId, product)
        EskoMobileAgent.endSession(userId, product)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // If user leaves app (home button), log idle
        EskoMobileAgent.trackIdle(userId, product, 0.5)
    }
}
