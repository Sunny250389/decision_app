package com.example.decisionapp.ui.model

/**
 * UI-level streaming events coming from SSE.
 * These are NOT domain models.
 */
sealed class StreamEvent {

    data class Status(
        val message: String
    ) : StreamEvent()

    data class OptionChunk(
        val option: OptionPartial
    ) : StreamEvent()

    data class Recommendation(
        val option: OptionPartial
    ) : StreamEvent()

    object Done : StreamEvent()
}

/**
 * Partial option while streaming.
 * Can be promoted to full Option later.
 */
data class OptionPartial(
    val title: String,
    val pros: List<String> = emptyList(),
    val cons: List<String> = emptyList(),
    val risk: String? = null
)