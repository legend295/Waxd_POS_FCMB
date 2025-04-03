package com.waxd.pos.fcmb.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.waxd.pos.fcmb.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class DataStoreWrapper(val context: Context) {

    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = context.getString(
            R.string.app_name
        )
    )

    fun dataStore() = context.dataStore

    suspend inline fun <reified T> setValue(key: Preferences.Key<T>, value: Any?) {
        value ?: return
        context.dataStore.edit {
            it[key] = value as T
        }
    }

    suspend inline fun <reified T> getValue(key: Preferences.Key<T>): Flow<T?> {
        return context.dataStore.data.catch {
            if (it is IOException) {
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map {
            it[key]
        }
    }

    suspend fun clearAllPreferences() {
        context.dataStore.edit {
            it.clear()
        }
    }

    object PreferencesKeys {
        val KEY_AUTH = stringPreferencesKey("${R.string.app_name}_key_auth_token")

    }

}