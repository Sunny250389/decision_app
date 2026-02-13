package com.example.decisionapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.PaddingValues
import com.example.decisionapp.model.DecisionUIState
import com.example.decisionapp.model.DecisionContext
import com.example.decisionapp.ui.components.*

@Composable
fun DecisionScreen(
    state: DecisionUIState,
    onDecisionChange: (String) -> Unit,
    onEvaluateDecision: () -> Unit,
    onGrowthChange: (Int) -> Unit,
    onStabilityChange: (Int) -> Unit,
    onRiskChange: (Int) -> Unit,
    contentPadding: PaddingValues
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp)
    ) {

        // ───────────────── Decision input ─────────────────
        DecisionInput(
            value = state.decisionText,
            onValueChange = onDecisionChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ───────────────── Counterfactual Context Section ─────────────────
        CounterfactualContextSection(
            context = state.context,
            onGrowthChange = onGrowthChange,
            onStabilityChange = onStabilityChange,
            onRiskChange = onRiskChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ───────────────── Constraints (future expansion) ─────────────────
        ConstraintsSection()

        Spacer(modifier = Modifier.height(16.dp))

        // ───────────────── Loading indicator ─────────────────
        if (state.isLoading) {
            LoadingBlock()
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ───────────────── Options ─────────────────
        state.options.forEach { option ->
            OptionCard(option)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ───────────────── Recommendation ─────────────────
        state.recommendation?.let { recommendation ->

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {

                Column(modifier = Modifier.padding(16.dp)) {

                    Text(
                        text = "Recommendation",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = recommendation,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Confidence: ${(state.confidenceScore * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = state.confidenceScore.toFloat()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun CounterfactualContextSection(
    context: DecisionContext,
    onGrowthChange: (Int) -> Unit,
    onStabilityChange: (Int) -> Unit,
    onRiskChange: (Int) -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = "Adjust Priorities (What If?)",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            ContextSlider(
                label = "Growth Priority",
                value = context.growthPriority,
                onValueChange = onGrowthChange
            )

            Spacer(modifier = Modifier.height(12.dp))

            ContextSlider(
                label = "Stability Priority",
                value = context.stabilityPriority,
                onValueChange = onStabilityChange
            )

            Spacer(modifier = Modifier.height(12.dp))

            ContextSlider(
                label = "Risk Tolerance",
                value = context.riskTolerance,
                onValueChange = onRiskChange
            )
        }
    }
}

@Composable
fun ContextSlider(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {

    Column {

        Text(text = "$label: $value")

        Slider(
            value = value.toFloat(),
            onValueChange = { newValue ->
                onValueChange(newValue.toInt())
            },
            valueRange = 1f..5f,
            steps = 3
        )
    }
}
