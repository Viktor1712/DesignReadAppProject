package com.design.readerapp

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object NotificationStore {
    private const val PREFS_NAME = "notifications_store"
    private const val KEY_LIST   = "notifications_json"
    private const val MAX_STORED = 50

    private val gson = Gson()

    fun load(context: Context): List<Notification> {
        val json = context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LIST, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Notification>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun prepend(context: Context, notification: Notification): List<Notification> {
        val updated = (listOf(notification) + load(context)).take(MAX_STORED)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LIST, gson.toJson(updated))
            .apply()
        return updated
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().remove(KEY_LIST).apply()
    }
}
