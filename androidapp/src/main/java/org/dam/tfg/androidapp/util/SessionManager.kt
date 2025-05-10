package org.dam.tfg.androidapp.util

import android.content.Context
import android.content.SharedPreferences
import org.dam.tfg.androidapp.util.Constants.PREF_NAME
import org.dam.tfg.androidapp.util.Constants.PREF_TOKEN
import org.dam.tfg.androidapp.util.Constants.PREF_USERNAME
import org.dam.tfg.androidapp.util.Constants.PREF_USER_TYPE
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveUserSession(token: String, username: String, userType: String) {
        val editor = sharedPreferences.edit()
        editor.putString(PREF_TOKEN, token)
        editor.putString(PREF_USERNAME, username)
        editor.putString(PREF_USER_TYPE, userType)
        editor.apply()
    }

    fun getUserToken(): String? {
        return sharedPreferences.getString(PREF_TOKEN, null)
    }

    fun getUsername(): String? {
        return sharedPreferences.getString(PREF_USERNAME, null)
    }

    fun getUserType(): String? {
        return sharedPreferences.getString(PREF_USER_TYPE, null)
    }

    fun isLoggedIn(): Boolean {
        return getUserToken() != null
    }

    fun clearSession() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    fun hashPassword(password: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val hashBytes = messageDigest.digest(password.toByteArray(StandardCharsets.UTF_8))
        val hexString = StringBuilder()

        for (byte in hashBytes) {
            hexString.append(String.format("%02x", byte))
        }
        return hexString.toString()
    }
}
