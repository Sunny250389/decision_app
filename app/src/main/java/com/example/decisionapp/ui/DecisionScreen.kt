package com.example.decisionapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.*
import com.example.decisionapp.model.DecisionUIState
import com.example.decisionapp.ui.components.*
import com.example.decisionapp.ui.components.SectionTitle
import com.example.decisionapp.ui.components.BulletItem

@Composable
fun DecisionScreen(
    state: DecisionUIState,
    onDecisionChange: (String) -> Unit,
    onEvaluateDecision: () -> Unit,
    contentPadding: PaddingValues = PaddingValues()
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

        // ───────────────── Constraints & context ─────────────────
        ConstraintsSection()

        Spacer(modifier = Modifier.height(16.dp))

        // ───────────────── Loading indicator ─────────────────
        if (state.isLoading) {
            LoadingBlock()
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ───────────────── Options (exploration phase) ─────────────────
        state.options.forEach { option ->
            OptionCard(option)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ───────────────── Audit: Key Factors ─────────────────
        if (state.keyFactors.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("Key Factors")
            state.keyFactors.forEach {
                BulletItem(it)
            }
        }

        // ───────────────── Audit: Assumptions ─────────────────
        if (state.assumptions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("Assumptions")
            state.assumptions.forEach {
                BulletItem(it)
            }
        }

        // ───────────────── Audit: Reversal Triggers ─────────────────
        if (state.reversalTriggers.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("This decision would change if")
            state.reversalTriggers.forEach {
                BulletItem(it)
            }
        }

        // ───────────────── FINAL Recommendation (authority) ─────────────────
        state.finalRecommendation?.let { recommendation ->
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

                    state.confidence?.let { confidence ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Confidence: ${(confidence * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(progress = confidence)
                    }
                }
            }
        }
    }
}