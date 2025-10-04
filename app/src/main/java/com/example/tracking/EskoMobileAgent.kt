package com.example.eskomobileagent.tracking

import android.app.Activity
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

object EskoMobileAgent {

    private var sessionStart: Long = 0

    fun startSession(activity: Activity, userId: String, product: String) {
        sessionStart = System.currentTimeMillis()
        sendEvent("session_start", userId, product, 0)
    }

    fun endSession(userId: String, product: String) {
        val duration = System.currentTimeMillis() - sessionStart
        sendEvent("session_end", userId, product, duration)
    }

    fun trackClick(userId: String, product: String, buttonName: String) {
        sendEvent("click_$buttonName", userId, product, 0)
    }

    private fun sendEvent(action: String, userId: String, product: String, duration: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("http://10.0.2.2:8080/api/events") // emulator localhost
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val json = """
                    {
                      "userId": "$userId",
                      "product": "$product",
                      "action": "$action",
                      "timestamp": ${System.currentTimeMillis()},
                      "duration": $duration
                    }
                """.trimIndent()

                conn.outputStream.use { os -> os.write(json.toByteArray()) }
                conn.inputStream.close()
                conn.disconnect()

                Log.d("EskoAgent", "Event sent: $json")

            } catch (e: Exception) {
                Log.e("EskoAgent", "Failed to send event", e)
            }
        }
    }
}
