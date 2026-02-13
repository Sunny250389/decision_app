package com.example.decisionapp.ui

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.decisionapp.ui.components.BottomActionBar
import com.example.decisionapp.viewmodel.DecisionViewModel

@Composable
fun DecisionAppRoot() {

    val viewModel: DecisionViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            BottomActionBar(
                enabled = uiState.decisionText.isNotBlank() && !uiState.isLoading,
                onEvaluate = { viewModel.evaluateDecision() }
            )
        }
    ) { paddingValues ->   // 🔑 THIS WAS MISSING

        DecisionScreen(
            state = uiState,
            onDecisionChange = { viewModel.updateDecision(it) },
            onEvaluateDecision = { viewModel.evaluateDecision() },
            onGrowthChange = { viewModel.updateGrowthPriority(it) },
            onStabilityChange = { viewModel.updateStabilityPriority(it) },
            onRiskChange = { viewModel.updateRiskTolerance(it) },
            contentPadding = paddingValues
        )
    }
}