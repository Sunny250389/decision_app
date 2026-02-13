package com.example.decisionapp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.decisionapp.model.Option

@Composable
fun RecommendationCard(option: Option) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Recommended",
                style = MaterialTheme.typography.titleLarge
            )
            Text(text = option.title)
            Text(text = "Risk: ${option.risk}")
        }
    }
}