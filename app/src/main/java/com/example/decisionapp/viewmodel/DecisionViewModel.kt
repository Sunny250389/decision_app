package com.example.decisionapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.decisionapp.engine.*
import com.example.decisionapp.model.DecisionContext
import com.example.decisionapp.model.DecisionUIState
import com.example.decisionapp.model.Option
import com.example.decisionapp.repository.DecisionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max

class DecisionViewModel(
    private val repository: DecisionRepository = DecisionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DecisionUIState())
    val uiState: StateFlow<DecisionUIState> = _uiState.asStateFlow()

    // Holds streaming options for recompute
    private val accumulatedOptions = mutableListOf<Option>()

    // -------------------------------------------------
    // MAIN ENTRY
    // -------------------------------------------------

    fun submitDecision(
        decisionText: String,
        constraints: List<String>
    ) {

        accumulatedOptions.clear()

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null,
            recommendation = null,
            options = emptyList(),
            confidenceScore = 0.0
        )

        viewModelScope.launch {

            repository.evaluateDecision(
                decisionText = decisionText,
                constraints = constraints
            ).collect { (event, payload) ->

                when (event) {

                    "option" -> {
                        val option = payload as? Option ?: return@collect

                        accumulatedOptions.add(option)
                        recomputeWithCurrentContext()
                    }

                    "recommendation" -> {
                        val recommendationText = payload as? String ?: ""

                        _uiState.value = _uiState.value.copy(
                            recommendation = recommendationText,
                            isLoading = false
                        )
                    }

                    "error" -> {
                        val errorMessage = payload as? String ?: "Unknown error"

                        _uiState.value = _uiState.value.copy(
                            error = errorMessage,
                            isLoading = false
                        )
                    }

                    "done" -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    // -------------------------------------------------
    // CORE RECOMPUTE ENGINE (Used by sliders)
    // -------------------------------------------------

    private fun recomputeWithCurrentContext() {

        if (accumulatedOptions.isEmpty()) return

        val ranked = applyDeterministicScoring(accumulatedOptions)
        val confidence = computeConfidence(ranked)

        _uiState.value = _uiState.value.copy(
            options = ranked,
            confidenceScore = confidence
        )
    }

    // -------------------------------------------------
    // SCORING
    // -------------------------------------------------

    private fun applyDeterministicScoring(
        options: List<Option>
    ): List<Option> {

        if (options.isEmpty()) return options

        val preferences = buildPreferencesFromContext()

        return ScoringEngine.rankOptions(
            options = options,
            preferences = preferences,
            riskSensitivity = deriveRiskSensitivity(),
            emotionalSensitivity = deriveEmotionalSensitivity()
        )
    }

    private fun buildPreferencesFromContext(): List<ContextPreference> {

        val ctx = _uiState.value.context

        return listOf(
            ContextPreference(AttributeDimension.UPSIDE, ctx.growthPriority.toDouble()),
            ContextPreference(AttributeDimension.STABILITY, ctx.stabilityPriority.toDouble()),
            ContextPreference(AttributeDimension.FLEXIBILITY, ctx.flexibilityPriority.toDouble()),
            ContextPreference(AttributeDimension.LEARNING_VALUE, ctx.learningPriority.toDouble()),
            ContextPreference(AttributeDimension.EFFORT, (6 - ctx.emotionalStress).toDouble())
        )
    }

    private fun deriveRiskSensitivity(): Double {
        val riskTolerance = _uiState.value.context.riskTolerance
        return (6 - riskTolerance) * 0.2
    }

    private fun deriveEmotionalSensitivity(): Double {
        val stress = _uiState.value.context.emotionalStress
        return stress * 0.2
    }

    // -------------------------------------------------
    // CONFIDENCE
    // -------------------------------------------------

    private fun computeConfidence(
        rankedOptions: List<Option>
    ): Double {

        if (rankedOptions.isEmpty()) return 0.0

        val separation = calculateNormalizedSeparation(rankedOptions)

        return ConfidenceCalculator.compute(
            inputCompleteness = 0.7,
            constraintClarity = 0.8,
            scoreSeparation = separation
        ).coerceIn(0.0, 1.0)
    }

    private fun calculateNormalizedSeparation(
        rankedOptions: List<Option>
    ): Double {

        if (rankedOptions.size < 2) return 0.5

        val top = rankedOptions[0].computedScore ?: 0.0
        val second = rankedOptions[1].computedScore ?: 0.0

        val diff = abs(top - second)
        val maxScore = max(abs(top), abs(second)).takeIf { it > 0 } ?: 1.0

        return (diff / maxScore).coerceIn(0.0, 1.0)
    }

    // -------------------------------------------------
    // UI BINDINGS
    // -------------------------------------------------

    fun updateDecision(text: String) {
        _uiState.value = _uiState.value.copy(
            decisionText = text
        )
    }

    fun evaluateDecision() {

        val currentText = _uiState.value.decisionText
        if (currentText.isBlank()) return

        submitDecision(
            decisionText = currentText,
            constraints = emptyList()
        )
    }

    // -------------------------------------------------
    // CONTEXT UPDATES (Auto-Recompute Enabled)
    // -------------------------------------------------

    fun updateGrowthPriority(value: Int) {
        updateContext(_uiState.value.context.copy(
            growthPriority = value.coerceIn(1, 5)
        ))
    }

    fun updateStabilityPriority(value: Int) {
        updateContext(_uiState.value.context.copy(
            stabilityPriority = value.coerceIn(1, 5)
        ))
    }

    fun updateRiskTolerance(value: Int) {
        updateContext(_uiState.value.context.copy(
            riskTolerance = value.coerceIn(1, 5)
        ))
    }

    fun updateEmotionalStress(value: Int) {
        updateContext(_uiState.value.context.copy(
            emotionalStress = value.coerceIn(1, 5)
        ))
    }

    private fun updateContext(newContext: DecisionContext) {

        _uiState.value = _uiState.value.copy(
            context = newContext
        )

        // Immediately recompute ranking
        recomputeWithCurrentContext()
    }
}
