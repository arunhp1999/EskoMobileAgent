package com.example.eskomobileagent

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.eskomobileagent.tracking.EskoMobileAgent

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Example: start session when activity is opened
        EskoMobileAgent.startSession(this, "U1", "MobileApp")

        // Example: simulate a button click
        // myButton.setOnClickListener {
        //     EskoMobileAgent.trackClick("U1", "MobileApp", "SubmitButton")
        // }
    }

    override fun onPause() {
        super.onPause()
        EskoMobileAgent.endSession("U1", "MobileApp")
    }
}
