package com.example.decisionapp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BottomActionBar(
    enabled: Boolean,
    onEvaluate: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = onEvaluate,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Evaluate Decision")
        }
    }
}