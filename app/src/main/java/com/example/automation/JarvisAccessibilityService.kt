package com.example.automation

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.util.Log

class JarvisAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Can be used to monitor UI changes and assist the user
        // Log.d("JarvisService", "Event: ${AccessibilityEvent.eventTypeToString(event?.eventType ?: 0)}")
    }

    override fun onInterrupt() {
        Log.d("JarvisService", "Service Interrupted")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("JarvisService", "Service Connected")
    }
}
