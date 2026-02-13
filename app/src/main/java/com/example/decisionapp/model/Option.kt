package com.example.decisionapp.model

import com.example.decisionapp.engine.AttributeDimension

data class Option(
    val id: String = "",
    val title: String,
    val description: String = "",
    val pros: List<String> = emptyList(),
    val cons: List<String> = emptyList(),
    val risk: String? = null,

    val dimensions: Map<AttributeDimension, Double> = emptyMap(),
    val riskLevel: Double = 0.5,
    val computedScore: Double? = null
)
