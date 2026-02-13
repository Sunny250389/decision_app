package com.example.decisionapp.model

data class DecisionUIState(
    val decisionText: String = "",
    val isLoading: Boolean = false,

    val options: List<Option> = emptyList(),

    // Legacy (optional, exploratory only)
    val recommendation: Option? = null,

    // Audit-authoritative
    val finalRecommendation: String? = null,
    val confidence: Float? = null,

    val keyFactors: List<String> = emptyList(),
    val assumptions: List<String> = emptyList(),
    val reversalTriggers: List<String> = emptyList(),
    val statusMessage: String? = null
)