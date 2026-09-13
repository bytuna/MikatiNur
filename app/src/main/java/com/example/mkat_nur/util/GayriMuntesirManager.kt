package com.example.mkat_nur.util

import android.content.Context
import android.util.Log

object GayriMuntesirManager {

    private const val USERS_FILE = "gayri_muntesir_users.txt"
    private const val CONTENT_FILE = "gayri_muntesir_dersleri.txt"

    fun isUserAllowed(context: Context, userEmail: String?): Boolean {
        if (userEmail.isNullOrBlank()) return false
        return try {
            val allowedEmails = context.assets.open(USERS_FILE)
                .bufferedReader()
                .readLines()
                .map { it.trim().lowercase() }
                .filter { it.isNotEmpty() && !it.startsWith("#") }

            userEmail.trim().lowercase() in allowedEmails
        } catch (e: Exception) {
            Log.e("GayriMuntesir", "Error reading allowed users file: ${e.message}")
            false
        }
    }

    fun getLessons(context: Context): List<String> {
        return try {
            context.assets.open(CONTENT_FILE)
                .bufferedReader()
                .use { it.readText() }
                .split("===")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        } catch (e: Exception) {
            Log.e("GayriMuntesir", "Error reading content file: ${e.message}")
            listOf("Gayri Münteşir içerikleri yüklenemedi.")
        }
    }
}
