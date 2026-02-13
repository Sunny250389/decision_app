package com.example.decisionapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.decisionapp.model.DecisionUIState
import com.example.decisionapp.model.Option
import com.example.decisionapp.repository.DecisionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DecisionViewModel : ViewModel() {

    private val repository = DecisionRepository()

    private val _uiState = MutableStateFlow(DecisionUIState())
    val uiState = _uiState.asStateFlow()

    fun updateDecision(text: String) {
        _uiState.update { it.copy(decisionText = text) }
    }

    fun evaluateDecision() {

        if (_uiState.value.isLoading) {
            Log.w("DecisionVM", "evaluateDecision ignored — already loading")
            return
        }

        Log.d("DecisionVM", "evaluateDecision() called")

        val decisionText = _uiState.value.decisionText

        _uiState.update {
            it.copy(
                isLoading = true,
                statusMessage = null,
                options = emptyList(),
                recommendation = null,
                finalRecommendation = null,
                confidence = null,
                keyFactors = emptyList(),
                assumptions = emptyList(),
                reversalTriggers = emptyList()
            )
        }

        viewModelScope.launch {
            try {
                repository.evaluateDecision(
                    decisionText = decisionText,
                    constraints = emptyList()
                ).collect { (event, payload) ->

                    when (event) {

                        /* ---------- STATUS ---------- */
                        "status" -> {
                            val message = payload as? String
                            Log.d("DecisionVM", "status: $message")
                            if (message != null) {
                                _uiState.update { it.copy(statusMessage = message) }
                            }
                        }

                        /* ---------- OPTIONS ---------- */
                        "option" -> {
                            if (payload is Option) {
                                _uiState.update {
                                    it.copy(options = it.options + payload)
                                }
                            }
                        }

                        "recommendation" -> {
                            if (payload is Option) {
                                _uiState.update {
                                    it.copy(recommendation = payload)
                                }
                            }
                        }

                        /* ---------- AUDIT EVENTS ---------- */
                        "audit:confidence" -> {
                            val value = (payload as? Number)?.toFloat()
                            if (value != null) {
                                _uiState.update { it.copy(confidence = value) }
                            }
                        }

                        "audit:key_factors" -> {
                            val list = payload as? List<*> ?: emptyList<Any>()
                            _uiState.update {
                                it.copy(keyFactors = list.filterIsInstance<String>())
                            }
                        }

                        "audit:assumptions" -> {
                            val list = payload as? List<*> ?: emptyList<Any>()
                            _uiState.update {
                                it.copy(assumptions = list.filterIsInstance<String>())
                            }
                        }

                        "audit:reversal_triggers" -> {
                            val list = payload as? List<*> ?: emptyList<Any>()
                            _uiState.update {
                                it.copy(reversalTriggers = list.filterIsInstance<String>())
                            }
                        }

                        /* ---------- 🔥 FINAL DECISION ---------- */
                        "decision_audit" -> {
                            val audit = payload as Map<*, *>

                            Log.d("DecisionVM", "decision_audit received")

                            _uiState.update {
                                it.copy(
                                    finalRecommendation =
                                        audit["final_recommendation"] as? String,
                                    confidence =
                                        (audit["confidence"] as? Number)?.toFloat(),
                                    keyFactors =
                                        (audit["key_factors"] as? List<*>)?.filterIsInstance<String>()
                                            ?: emptyList(),
                                    assumptions =
                                        (audit["assumptions"] as? List<*>)?.filterIsInstance<String>()
                                            ?: emptyList(),
                                    reversalTriggers =
                                        (audit["reversal_triggers"] as? List<*>)?.filterIsInstance<String>()
                                            ?: emptyList(),
                                    isLoading = false,
                                    statusMessage = null
                                )
                            }
                        }

                        /* ---------- DONE ---------- */
                        "done" -> {
                            Log.d("DecisionVM", "stream done")
                            _uiState.update { it.copy(isLoading = false) }
                        }

                        /* ---------- ERROR ---------- */
                        "error" -> {
                            Log.e("DecisionVM", "backend error: $payload")
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    finalRecommendation =
                                        "Something went wrong while analyzing the decision."
                                )
                            }
                        }

                        else -> {
                            Log.d("DecisionVM", "Ignored event: $event")
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("DecisionVM", "Decision stream failed", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        finalRecommendation = "Decision analysis failed. Please retry."
                    )
                }
            }
        }
    }
}