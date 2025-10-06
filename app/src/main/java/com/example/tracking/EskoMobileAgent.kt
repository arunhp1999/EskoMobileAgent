package com.example.eskomobileagent.tracking

import android.content.Context
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object EskoMobileAgent {
    private const val BACKEND_URL = "http://10.0.2.2:8080/api/events"

    private var lastInteractionTime: Long = System.currentTimeMillis()
    private var heartbeatJob: Job? = null
    private var idleCheckJob: Job? = null
    private var isSessionActive = false

    fun startSession(context: Context, userId: String, product: String) {
        println("📡 Starting session...")
        isSessionActive = true
        lastInteractionTime = System.currentTimeMillis()

        sendEvent("session_start", userId, product, null)

        // Periodic heartbeat
        heartbeatJob = CoroutineScope(Dispatchers.IO).launch {
            while (isSessionActive) {
                delay(30_000) // every 30 seconds
                sendEvent("heartbeat", userId, product, null)
            }
        }

        // Idle detector (checks every 10 seconds)
        idleCheckJob = CoroutineScope(Dispatchers.IO).launch {
            while (isSessionActive) {
                delay(10_000)
                maybeSendIdleEvent(userId, product)
            }
        }
    }

    fun endSession(userId: String, product: String) {
        println("📡 Ending session...")
        isSessionActive = false
        heartbeatJob?.cancel()
        idleCheckJob?.cancel()
        sendEvent("session_end", userId, product, null)
    }

    fun recordUserAction(userId: String, product: String, action: String) {
        lastInteractionTime = System.currentTimeMillis()
        println("🎯 User performed action: $action")
        sendEvent(action, userId, product, null)
    }

    private fun maybeSendIdleEvent(userId: String, product: String) {
        val now = System.currentTimeMillis()
        val idleDurationMs = now - lastInteractionTime

        if (idleDurationMs > 60_000) { // user inactive > 1 minute
            val idleMinutes = idleDurationMs / 60000.0
            println("💤 User idle for $idleMinutes min, sending event...")
            sendEvent("idle", userId, product, idleMinutes)
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
