package com.hp77.linkstash.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

object UIAnomalyDetector {
    private val stateTransitions = ConcurrentHashMap<String, StateTransition>()
    private val _anomalyDetected = MutableStateFlow<UIAnomaly?>(null)
    val anomalyDetected: StateFlow<UIAnomaly?> = _anomalyDetected

    data class StateTransition(
        val startTime: Long = System.currentTimeMillis(),
        var endTime: Long? = null,
        var isCompleted: Boolean = false
    )

    data class UIAnomaly(
        val component: String,
        val description: String,
        val duration: Long,
        val expectedDuration: Long = 10000 // 1 second default threshold
    )

    fun startTransition(componentId: String) {
        stateTransitions[componentId] = StateTransition()
    }

    fun endTransition(componentId: String, expectedDuration: Long = 1.seconds.inWholeMilliseconds) {
        val transition = stateTransitions[componentId] ?: return
        
        if (!transition.isCompleted) {
            transition.endTime = System.currentTimeMillis()
            transition.isCompleted = true
            
            val duration = transition.endTime!! - transition.startTime
            
            // Check if transition took longer than expected
            if (duration > expectedDuration) {
                _anomalyDetected.value = UIAnomaly(
                    component = componentId,
                    description = "UI transition took longer than expected ($duration ms > $expectedDuration ms)",
                    duration = duration,
                    expectedDuration = expectedDuration
                )
            }
        }
        
        // Cleanup
        stateTransitions.remove(componentId)
    }

    fun reportStuckTransition(componentId: String, description: String) {
        val transition = stateTransitions[componentId]
        if (transition != null && !transition.isCompleted) {
            val duration = System.currentTimeMillis() - transition.startTime
            _anomalyDetected.value = UIAnomaly(
                component = componentId,
                description = description,
                duration = duration
            )
        }
    }

    fun reset() {
        stateTransitions.clear()
        _anomalyDetected.value = null
    }
}
