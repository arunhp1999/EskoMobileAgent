package com.example.eskomobileagent.tracking

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object EskoMobileAgent {
    private const val BACKEND_URL = "http://10.0.2.2:8080/api/events"
    private var lastInteractionTime: Long = System.currentTimeMillis()

    fun startSession(context: Context, userId: String, product: String) {
        println("📡 Sending session_start event...")
        lastInteractionTime = System.currentTimeMillis()
        sendEvent("session_start", userId, product, null)
    }

    fun endSession(userId: String, product: String) {
        println("📡 Sending session_end event...")
        sendEvent("session_end", userId, product, null)
    }

    fun trackIdle(userId: String, product: String, durationMinutes: Double) {
        println("💤 Sending idle event for $durationMinutes minutes...")
        sendEvent("idle", userId, product, durationMinutes)
    }

    fun recordUserAction(userId: String, product: String, action: String) {
        lastInteractionTime = System.currentTimeMillis()
        println("🎯 User performed action: $action")
        sendEvent(action, userId, product, null)
    }

    fun maybeSendIdleEvent(userId: String, product: String) {
        val now = System.currentTimeMillis()
        val idleDurationMs = now - lastInteractionTime
        val idleMinutes = idleDurationMs / 60000.0

        // Send idle event if user inactive for > 30 seconds
        if (idleDurationMs > 30_000) {
            trackIdle(userId, product, idleMinutes)
            lastInteractionTime = now
        }
    }

    private fun sendEvent(type: String, userId: String, product: String, duration: Double?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(BACKEND_URL)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                val payload = JSONObject()
                payload.put("eventType", type)
                payload.put("userId", userId)
                payload.put("product", product)
                if (duration != null) payload.put("duration", duration)

                conn.outputStream.use { it.write(payload.toString().toByteArray()) }

                val response = conn.inputStream.bufferedReader().use { it.readText() }
                println("✅ Sent event: $payload | Response: $response")

                conn.disconnect()
            } catch (e: Exception) {
                println("❌ Failed to send event: ${e.message}")
            }
        }
    }
}
