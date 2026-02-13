package com.example.decisionapp.model

data class Option(
    val title: String,
    val pros: List<String>,
    val cons: List<String>,
    val risk: String
)