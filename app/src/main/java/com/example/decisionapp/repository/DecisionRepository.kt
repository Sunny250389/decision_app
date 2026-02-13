package com.example.decisionapp.repository

import com.example.decisionapp.model.Option
import com.example.decisionapp.network.SseDecisionClient
import com.example.decisionapp.network.toStringList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject

class DecisionRepository {

    private val client = SseDecisionClient()

    fun evaluateDecision(
        decisionText: String,
        constraints: List<String>
    ): Flow<Pair<String, Any>> =
        client.streamDecision(decisionText, constraints)
            .map { sse ->
                val event = sse.first
                val rawData = sse.second

                when (event) {

                    "status" -> {
                        val message =
                            rawData.optJSONObject("data")?.optString("message")
                                ?: rawData.optString("message")
                        event to message
                    }

                    "option", "recommendation" -> {
                        if (!rawData.has("title")) {
                            event to rawData
                        } else {
                            event to Option(
                                title = rawData.optString("title"),
                                pros = rawData.optJSONArray("pros")?.toStringList() ?: emptyList(),
                                cons = rawData.optJSONArray("cons")?.toStringList() ?: emptyList(),
                                risk = rawData.optString("risk")
                            )
                        }
                    }

                    "audit:key_factors",
                    "audit:assumptions",
                    "audit:reversal_triggers" -> {
                        val list = rawData.optJSONArray("data")?.toStringList() ?: emptyList()
                        event to list
                    }

                    "audit:recommendation" -> {
                        event to rawData.optString("data")
                    }

                    "audit:confidence" -> {
                        event to rawData.optDouble("data").toFloat()
                    }

                    "error" -> {
                        event to rawData.optString("message")
                    }

                    "done" -> {
                        event to Unit
                    }

                    else -> {
                        event to rawData
                    }
                } as Pair<String, Any>
            }
}