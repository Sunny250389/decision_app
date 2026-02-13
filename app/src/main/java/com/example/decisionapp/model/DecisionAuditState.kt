package com.example.decisionapp.model

data class DecisionAuditState(
    val statusMessage: String? = null,

    val keyFactors: List<String> = emptyList(),
    val assumptions: List<String> = emptyList(),
    val reversalTriggers: List<String> = emptyList(),

    val recommendation: String? = null,
    val confidence: Float? = null,

    val isComplete: Boolean = false,
    val error: String? = null
)