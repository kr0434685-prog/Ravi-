package com.example.ai

import kotlinx.serialization.Serializable

@Serializable
data class ActionPlan(
    val action: String,
    val url: String? = null,
    val package_name: String? = null,
    val query: String? = null,
    val app: String? = null,
    val phone_number: String? = null,
    val message: String? = null,
    val title: String? = null,
    val time: String? = null,
    val requires_confirmation: Boolean = false,
    val response: String
)

object ActionTypes {
    const val OPEN_APP = "OPEN_APP"
    const val OPEN_URL = "OPEN_URL"
    const val GOOGLE_SEARCH = "GOOGLE_SEARCH"
    const val YOUTUBE_SEARCH = "YOUTUBE_SEARCH"
    const val MAP_SEARCH = "MAP_SEARCH"
    const val MAKE_PHONE_CALL = "MAKE_PHONE_CALL"
    const val SEND_MESSAGE = "SEND_MESSAGE"
    const val CREATE_REMINDER = "CREATE_REMINDER"
    const val OPEN_SETTINGS = "OPEN_SETTINGS"
    const val NO_ACTION = "NO_ACTION"
    const val SHOW_MESSAGE = "SHOW_MESSAGE"
}
