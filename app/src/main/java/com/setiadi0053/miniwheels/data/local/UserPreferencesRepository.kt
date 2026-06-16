package com.setiadi0053.miniwheels.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_PHOTO = stringPreferencesKey("user_photo")
    }

    val userToken: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[PreferencesKeys.AUTH_TOKEN]
        }

    suspend fun saveSession(token: String, name: String, email: String, photoUrl: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTH_TOKEN] = token
            preferences[PreferencesKeys.USER_NAME] = name
            preferences[PreferencesKeys.USER_EMAIL] = email
            preferences[PreferencesKeys.USER_PHOTO] = photoUrl
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    val userData: Flow<Map<String, String?>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            mapOf(
                "name" to preferences[PreferencesKeys.USER_NAME],
                "email" to preferences[PreferencesKeys.USER_EMAIL],
                "photo" to preferences[PreferencesKeys.USER_PHOTO]
            )
        }
}
