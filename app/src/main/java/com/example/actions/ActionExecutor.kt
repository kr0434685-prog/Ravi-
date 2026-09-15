package com.example.actions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.provider.AlarmClock
import com.example.ai.ActionPlan
import com.example.ai.ActionTypes
import android.widget.Toast

class ActionExecutor(private val context: Context) {

    fun execute(actionPlan: ActionPlan) {
        when (actionPlan.action) {
            ActionTypes.OPEN_APP -> openApp(actionPlan.app ?: actionPlan.package_name)
            ActionTypes.OPEN_URL -> openUrl(actionPlan.url)
            ActionTypes.GOOGLE_SEARCH -> googleSearch(actionPlan.query)
            ActionTypes.YOUTUBE_SEARCH -> youtubeSearch(actionPlan.query)
            ActionTypes.MAP_SEARCH -> mapSearch(actionPlan.query)
            ActionTypes.OPEN_SETTINGS -> openSettings(Settings.ACTION_SETTINGS)
            ActionTypes.MAKE_PHONE_CALL -> makePhoneCall(actionPlan.phone_number)
            ActionTypes.SEND_MESSAGE -> sendMessage(actionPlan.phone_number, actionPlan.message)
            ActionTypes.CREATE_REMINDER -> createReminder(actionPlan.title, actionPlan.time)
            ActionTypes.SHOW_MESSAGE -> showMessage(actionPlan.message ?: actionPlan.response)
            ActionTypes.NO_ACTION -> { /* Do nothing */ }
            else -> showMessage("Action not supported: ${actionPlan.action}")
        }
    }

    fun openYouTube(query: String? = null) {
        if (!query.isNullOrBlank()) {
            youtubeSearch(query)
            return
        }
        val launchIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
        if (launchIntent != null) {
            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(launchIntent)
        } else {
            openUrl("https://m.youtube.com")
        }
    }

    fun openGoogle(query: String? = null) {
        if (!query.isNullOrBlank()) {
            googleSearch(query)
            return
        }
        val launchIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.googlequicksearchbox")
        if (launchIntent != null) {
            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(launchIntent)
        } else {
            openUrl("https://www.google.com")
        }
    }

    fun openChrome(url: String? = null) {
        val targetUrl = url ?: "https://www.google.com"
        val chromeIntent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
            setPackage("com.android.chrome")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(chromeIntent)
        } catch (e: Exception) {
            openUrl(targetUrl)
        }
    }

    fun openMaps(query: String? = null) {
        if (!query.isNullOrBlank()) {
            mapSearch(query)
            return
        }
        val mapsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=")).apply {
            setPackage("com.google.android.apps.maps")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(mapsIntent)
        } catch (e: Exception) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.apps.maps")
            if (launchIntent != null) {
                launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(launchIntent)
            } else {
                openUrl("https://maps.google.com")
            }
        }
    }

    private fun openApp(appName: String?) {
        if (appName == null) return
        val lower = appName.lowercase().trim()
        when {
            lower.contains("youtube") -> openYouTube()
            lower.contains("chrome") -> openChrome()
            lower.contains("google") -> openGoogle()
            lower.contains("map") -> openMaps()
            else -> {
                val packageName = getPackageNameForApp(appName)
                val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
                if (launchIntent != null) {
                    launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(launchIntent)
                } else {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "App not found: $appName", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun getPackageNameForApp(appName: String): String {
        return when (appName.lowercase()) {
            "chrome" -> "com.android.chrome"
            "youtube" -> "com.google.android.youtube"
            "whatsapp" -> "com.whatsapp"
            "instagram" -> "com.instagram.android"
            "maps" -> "com.google.android.apps.maps"
            "google" -> "com.google.android.googlequicksearchbox"
            "settings" -> "com.android.settings"
            else -> appName // Assume it's a package name if not matched
        }
    }

    private fun openUrl(url: String?) {
        if (url == null) return
        val uri = if (!url.startsWith("http")) "https://$url" else url
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    private fun googleSearch(query: String?) {
        if (query == null) return
        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra("query", query)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    private fun youtubeSearch(query: String?) {
        if (query == null) return
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$query")).apply {
            setPackage("com.google.android.youtube")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            openUrl("https://www.youtube.com/results?search_query=$query")
        }
    }

    private fun mapSearch(query: String?) {
        if (query == null) return
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$query")).apply {
            setPackage("com.google.android.apps.maps")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    private fun openSettings(settingsAction: String) {
        val intent = Intent(settingsAction).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    private fun makePhoneCall(phoneNumber: String?) {
        if (phoneNumber == null) return
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    private fun sendMessage(phoneNumber: String?, message: String?) {
        if (phoneNumber == null || message == null) return
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phoneNumber")).apply {
            putExtra("sms_body", message)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    private fun createReminder(title: String?, time: String?) {
        if (title == null) return
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_MESSAGE, title)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    private fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}
