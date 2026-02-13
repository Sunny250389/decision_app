package com.example.decisionapp.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun BulletItem(text: String) {
    Row(modifier = Modifier.padding(start = 8.dp, top = 4.dp)) {
        Text("• ")
        Text(text)
    }
}