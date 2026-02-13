package com.example.decisionapp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.decisionapp.model.Option

@Composable
fun OptionCard(option: Option) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = option.title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Pros: ${option.pros.joinToString()}")
            Text(text = "Cons: ${option.cons.joinToString()}")
            Text(text = "Risk: ${option.risk}")
        }
    }
}