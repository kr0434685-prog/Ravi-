package com.example.ai

import kotlinx.serialization.json.Json
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommandParser(private val geminiManager: GeminiManager) {
    private val json = Json { ignoreUnknownKeys = true }

    private val systemInstruction = """
        You are JARVIS, a personal AI voice assistant for Android.
        You are ultra-smart, proactive, concise, and helpful.
        Convert the user's natural language command into a structured JSON ACTION PLAN.
        
        Supported Actions:
        - OPEN_APP: requires 'app' (name of app like Chrome, YouTube, Maps, Google) or 'package_name'
        - OPEN_URL: requires 'url'
        - GOOGLE_SEARCH: requires 'query'
        - YOUTUBE_SEARCH: requires 'query'
        - MAP_SEARCH: requires 'query'
        - MAKE_PHONE_CALL: requires 'phone_number'. Set 'requires_confirmation' to true.
        - SEND_MESSAGE: requires 'phone_number' and 'message'. Set 'requires_confirmation' to true.
        - CREATE_REMINDER: requires 'title' and optional 'time'.
        - OPEN_SETTINGS: Opens main settings.
        - NO_ACTION: For conversation or answers.
        
        Rules:
        1. Always respond in the language the user spoke (Hindi, Hinglish, or English).
        2. Keep the 'response' clear, helpful, and concise (1-2 sentences).
        3. If you can't find a phone number for CALL/SMS, ask for it in the 'response'.
        
        Output MUST be a single valid JSON object matching the ActionPlan schema.
    """.trimIndent()

    suspend fun parseCommand(command: String, customApiKey: String? = null, languagePref: String = "hi-IN"): ActionPlan {
        val trimmed = command.trim()
        val lower = trimmed.lowercase(Locale.ROOT)

        // 1. Instant local offline pattern matching for high reliability
        val localPlan = tryMatchLocalIntent(lower, trimmed, languagePref)
        if (localPlan != null) {
            return localPlan
        }

        // 2. Query Gemini API for natural conversation and complex commands
        return try {
            val jsonString = geminiManager.generateResponse(command, systemInstruction, customApiKey)
            if (jsonString != null) {
                Log.d("CommandParser", "Gemini Response: $jsonString")
                json.decodeFromString<ActionPlan>(jsonString)
            } else {
                generateFallbackResponse(lower, languagePref)
            }
        } catch (e: Exception) {
            Log.e("CommandParser", "Error parsing command via Gemini", e)
            generateFallbackResponse(lower, languagePref)
        }
    }

    private fun tryMatchLocalIntent(lower: String, original: String, languagePref: String): ActionPlan? {
        val isHindi = languagePref.startsWith("hi") || lower.contains("kholo") || lower.contains("karo") || lower.contains("chalao")

        // YouTube Intent
        if (lower.contains("youtube")) {
            val query = extractQuery(lower, listOf("search", "play", "dhoondo", "chalao", "dikhao", "on youtube", "youtube par", "youtube pe"))
            return if (query.isNotBlank()) {
                ActionPlan(
                    action = ActionTypes.YOUTUBE_SEARCH,
                    query = query,
                    response = if (isHindi) "YouTube पर $query खोजा जा रहा है" else "Searching YouTube for $query"
                )
            } else {
                ActionPlan(
                    action = ActionTypes.OPEN_APP,
                    app = "youtube",
                    package_name = "com.google.android.youtube",
                    response = if (isHindi) "YouTube खोला जा रहा है" else "Opening YouTube"
                )
            }
        }

        // Google Intent
        if (lower.startsWith("google") || lower.contains("search on google") || lower.contains("google search") || lower.contains("google par")) {
            val query = extractQuery(lower, listOf("google search", "search on google", "search", "google par", "google", "kholo", "open"))
            return if (query.isNotBlank()) {
                ActionPlan(
                    action = ActionTypes.GOOGLE_SEARCH,
                    query = query,
                    response = if (isHindi) "Google पर $query खोज रहे हैं" else "Searching Google for $query"
                )
            } else {
                ActionPlan(
                    action = ActionTypes.OPEN_APP,
                    app = "google",
                    package_name = "com.google.android.googlequicksearchbox",
                    response = if (isHindi) "Google खोला जा रहा है" else "Opening Google"
                )
            }
        }

        // Chrome Intent
        if (lower.contains("chrome") || lower.contains("browser")) {
            return ActionPlan(
                action = ActionTypes.OPEN_APP,
                app = "chrome",
                package_name = "com.android.chrome",
                response = if (isHindi) "Google Chrome खोला जा रहा है" else "Opening Google Chrome"
            )
        }

        // Maps Intent
        if (lower.contains("map") || lower.contains("maps") || lower.contains("rasta") || lower.contains("location") || lower.contains("navigate")) {
            val query = extractQuery(lower, listOf("direction to", "directions to", "navigate to", "route to", "where is", "maps for", "map of", "kahan hai", "ka rasta"))
            return if (query.isNotBlank()) {
                ActionPlan(
                    action = ActionTypes.MAP_SEARCH,
                    query = query,
                    response = if (isHindi) "Maps पर $query का रास्ता देख रहे हैं" else "Finding directions to $query on Google Maps"
                )
            } else {
                ActionPlan(
                    action = ActionTypes.OPEN_APP,
                    app = "maps",
                    package_name = "com.google.android.apps.maps",
                    response = if (isHindi) "Google Maps खोला जा रहा है" else "Opening Google Maps"
                )
            }
        }

        // Settings Intent
        if (lower.contains("setting") || lower.contains("settings")) {
            return ActionPlan(
                action = ActionTypes.OPEN_SETTINGS,
                response = if (isHindi) "सेटिंग्स खोली जा रही हैं" else "Opening Settings"
            )
        }

        // Phone call intent
        if (lower.startsWith("call") || lower.contains("phone lagao") || lower.contains("call karo")) {
            val digits = original.filter { it.isDigit() }
            if (digits.length >= 3) {
                return ActionPlan(
                    action = ActionTypes.MAKE_PHONE_CALL,
                    phone_number = digits,
                    requires_confirmation = true,
                    response = if (isHindi) "क्या आप $digits पर कॉल करना चाहते हैं?" else "Do you want to call $digits?"
                )
            }
        }

        // Time query
        if (lower.contains("time") || lower.contains("samay") || lower.contains("kitne baje")) {
            val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            return ActionPlan(
                action = ActionTypes.SHOW_MESSAGE,
                response = if (isHindi) "अभी का समय $timeStr है।" else "The current time is $timeStr."
            )
        }

        // Date query
        if (lower.contains("date") || lower.contains("tarikh") || lower.contains("aaj ka din")) {
            val dateStr = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date())
            return ActionPlan(
                action = ActionTypes.SHOW_MESSAGE,
                response = if (isHindi) "आज की तारीख $dateStr है।" else "Today is $dateStr."
            )
        }

        // Identity / Greetings
        if (lower == "hello" || lower == "hi" || lower == "hey" || lower.contains("namaste") || lower.contains("namaskar")) {
            return ActionPlan(
                action = ActionTypes.SHOW_MESSAGE,
                response = if (isHindi) "नमस्ते! मैं जार्विस हूँ, आपका निजी AI असिस्टेंट। मैं आपकी क्या मदद करूँ?" else "Hello! I am JARVIS, your AI assistant. How can I help you today?"
            )
        }

        if (lower.contains("who are you") || lower.contains("tum kaun ho") || lower.contains("aap kaun ho") || lower.contains("tera naam")) {
            return ActionPlan(
                action = ActionTypes.SHOW_MESSAGE,
                response = if (isHindi) "मैं जार्विस हूँ, आपका पर्सनल AI वॉइस असिस्टेंट। मैं ऐप्स खोलने, सर्च करने और आपके आदेशों का पालन करने के लिए तैयार हूँ।" else "I am JARVIS, your personal Android voice assistant. I can open apps, search YouTube, Maps, Google, and assist you with commands."
            )
        }

        return null
    }

    private fun extractQuery(text: String, triggers: List<String>): String {
        var query = text
        for (trigger in triggers) {
            val idx = query.indexOf(trigger)
            if (idx != -1) {
                query = query.substring(idx + trigger.length).trim()
            }
        }
        return query.replace("youtube", "").replace("google", "").replace("maps", "").trim()
    }

    private fun generateFallbackResponse(lower: String, languagePref: String): ActionPlan {
        val isHindi = languagePref.startsWith("hi")
        return ActionPlan(
            action = ActionTypes.SHOW_MESSAGE,
            response = if (isHindi) {
                "माफ़ कीजिये, मैं इसे समझ नहीं पाया। आप 'YouTube खोलो', 'Google सर्च', या 'Maps खोलो' जैसे आदेश दे सकते हैं।"
            } else {
                "I didn't quite catch that. You can say 'Open YouTube', 'Search Google for weather', or 'Open Maps'."
            }
        )
    }
}
