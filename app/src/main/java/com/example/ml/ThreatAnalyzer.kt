package com.example.ml

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

enum class ThreatLevel {
    SAFE, WARNING, DANGER
}

data class ThreatAlert(
    val level: ThreatLevel,
    val source: String,
    val message: String,
    val confidence: Int
)

class ThreatAnalyzer {
    // Simulated On-Device Inference Stream
    fun observeThreats(): Flow<ThreatAlert> = flow {
        // Initial Safe state
        emit(ThreatAlert(ThreatLevel.SAFE, "Audio Monitor", "Listening for deepfake anomalies...", 100))
        delay(5000)
        
        // Mock a phishing context warning
        emit(ThreatAlert(ThreatLevel.WARNING, "Context Analyzer", "Detected urgency markers: 'wire transfer', 'immediate action'.", 65))
        delay(8000)
        
        // Mock a deepfake detection
        emit(ThreatAlert(ThreatLevel.DANGER, "Acoustic Fingerprint", "Synthetic voice anomaly detected! Frequency ceiling matched generic AI.", 92))
        delay(6000)
        
        // Return to safe
        emit(ThreatAlert(ThreatLevel.SAFE, "System", "Call ended. Monitoring paused.", 100))
    }
}
