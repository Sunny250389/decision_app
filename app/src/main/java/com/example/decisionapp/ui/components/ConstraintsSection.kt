package com.example.decisionapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp

@Composable
fun ConstraintsSection() {
    var expanded by remember { mutableStateOf(false) }

    Column {
        TextButton(onClick = { expanded = !expanded }) {
            Text("Constraints & Context")
        }

        AnimatedVisibility(visible = expanded) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = true,
                    onClick = {},
                    label = { Text("Medium Term") }
                )
                FilterChip(
                    selected = true,
                    onClick = {},
                    label = { Text("Medium Risk") }
                )
                FilterChip(
                    selected = true,
                    onClick = {},
                    label = { Text("Growth") }
                )
            }
        }
    }
}