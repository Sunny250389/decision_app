package com.example.decisionapp.network

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import org.json.JSONArray
import org.json.JSONObject

class SseDecisionClient {

    private val client = OkHttpClient()

    fun streamDecision(
        decisionText: String,
        constraints: List<String>
    ) = callbackFlow {

        val jsonBody = JSONObject().apply {
            put("decision_text", decisionText)
            put("constraints", JSONArray())
        }

        val request = Request.Builder()
            .url("http://10.0.2.2:8000/decision/evaluate") // emulator → localhost
            .post(jsonBody.toString().
            toRequestBody("application/json".toMediaType()))
            .build()

        val eventSource = EventSources.createFactory(client)
            .newEventSource(request, object : EventSourceListener() {

                override fun onEvent(
                    source: EventSource,
                    id: String?,
                    type: String?,
                    data: String
                ) {
                    try {
                        val payload = JSONObject(data)
                        trySend(type to payload)
                    } catch (e: Exception) {
                        // ignore malformed chunks
                    }
                }

                override fun onClosed(source: EventSource) {
                    close()
                }

                override fun onFailure(
                    source: EventSource,
                    t: Throwable?,
                    response: Response?
                ) {
                    close(t)
                }
            })

        awaitClose { eventSource.cancel() }
    }
}