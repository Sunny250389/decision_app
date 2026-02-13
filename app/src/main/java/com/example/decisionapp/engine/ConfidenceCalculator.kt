package com.example.decisionapp.engine

object ConfidenceCalculator {

    fun compute(
        inputCompleteness: Double,
        constraintClarity: Double,
        scoreSeparation: Double
    ): Double {

        return (0.4 * inputCompleteness) +
                (0.3 * constraintClarity) +
                (0.3 * scoreSeparation)
    }
}
