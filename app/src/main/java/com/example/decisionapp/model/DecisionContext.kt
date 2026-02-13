package com.example.decisionapp.model

data class DecisionContext(

    // 1–5 scale (1 = low importance, 5 = high importance)
    val growthPriority: Int = 3,
    val stabilityPriority: Int = 3,
    val flexibilityPriority: Int = 3,
    val learningPriority: Int = 3,

    // 1–5 scale
    val riskTolerance: Int = 3,

    // Emotional stress (1 = calm, 5 = highly stressed)
    val emotionalStress: Int = 3
)
