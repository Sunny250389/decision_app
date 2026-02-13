package com.example.decisionapp.repository

import com.example.decisionapp.model.Option
import com.example.decisionapp.network.SseDecisionClient
import com.example.decisionapp.network.toStringList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import com.example.decisionapp.engine.AttributeDimension


class DecisionRepository {

    private val client = SseDecisionClient()

    /**
     * Streams decision events from backend.
     *
     * Emits Pair(eventType, payload)
     *
     * eventType examples:
     *  - status
     *  - option
     *  - recommendation
     *  - audit:key_factors
     *  - audit:confidence
     *  - error
     *  - done
     */
    fun evaluateDecision(
        decisionText: String,
        constraints: List<String>
    ): Flow<Pair<String, Any>> {

        return client.streamDecision(decisionText, constraints)
            .map { sse ->

                val event = sse.first
                val rawData = sse.second

                when (event) {

                    "status" -> {
                        val message =
                            rawData.optJSONObject("data")?.optString("message")
                                ?: rawData.optString("message")

                        event to (message ?: "")
                    }

                    "option" -> {
                        event to parseOption(rawData)
                    }

                    "recommendation" -> {
                        event to rawData.optString("data")
                    }

                    "audit:key_factors",
                    "audit:assumptions",
                    "audit:reversal_triggers" -> {
                        val list =
                            rawData.optJSONArray("data")?.toStringList()
                                ?: emptyList()

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

    // -------------------------------------------------
    // PRIVATE HELPERS
    // -------------------------------------------------

    private fun parseOption(rawData: JSONObject): Option {

        val dimensionsJson = rawData.optJSONObject("dimensions")

        val dimensionMap = mutableMapOf<AttributeDimension, Double>()

        dimensionsJson?.let { json ->

            AttributeDimension.values().forEach { dimension ->

                val rawValue = json.optDouble(dimension.name, 0.5)

                val safeValue = rawValue
                    .takeIf { !it.isNaN() }
                    ?.coerceIn(0.0, 1.0)
                    ?: 0.5

                dimensionMap[dimension] = safeValue
            }
        }

        return Option(
            id = rawData.optString("id", ""),
            title = rawData.optString("title"),
            description = rawData.optString("description", ""),
            pros = rawData.optJSONArray("pros")?.toStringList() ?: emptyList(),
            cons = rawData.optJSONArray("cons")?.toStringList() ?: emptyList(),
            risk = rawData.optString("risk"),
            dimensions = dimensionMap,
            riskLevel = parseRiskLevel(rawData.optString("risk"))
        )
    }


    private fun parseRiskLevel(risk: String?): Double {
        return when (risk?.lowercase()) {
            "low" -> 0.2
            "medium" -> 0.5
            "high" -> 0.8
            else -> 0.5
        }
    }

}
