package com.example.decisionapp.engine

import com.example.decisionapp.model.Option

object ScoringEngine {

    fun scoreOption(
        option: Option,
        preferences: List<ContextPreference>,
        riskSensitivity: Double,
        emotionalSensitivity: Double
    ): Double {

        val baseScore = preferences.sumOf { pref ->
            val value = option.dimensions[pref.dimension] ?: 0.0
            pref.weight * value
        }

        val riskPenalty = option.riskLevel * riskSensitivity

        val emotionalCost =
            option.dimensions[AttributeDimension.EMOTIONAL_COST] ?: 0.0

        val emotionalPenalty = emotionalCost * emotionalSensitivity

        return baseScore - riskPenalty - emotionalPenalty
    }

    fun rankOptions(
        options: List<Option>,
        preferences: List<ContextPreference>,
        riskSensitivity: Double,
        emotionalSensitivity: Double
    ): List<Option> {

        return options
            .map { option ->
                val score = scoreOption(
                    option,
                    preferences,
                    riskSensitivity,
                    emotionalSensitivity
                )
                option.copy(computedScore = score)
            }
            .sortedByDescending { it.computedScore }
    }
}
