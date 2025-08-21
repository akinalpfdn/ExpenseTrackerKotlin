package com.example.expensetrackerkotlin.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {
    companion object {
        private val DEFAULT_CURRENCY_KEY = stringPreferencesKey("default_currency")
        private val DAILY_LIMIT_KEY = stringPreferencesKey("daily_limit")
        private val MONTHLY_LIMIT_KEY = stringPreferencesKey("monthly_limit")
    }

    val defaultCurrency: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DEFAULT_CURRENCY_KEY] ?: "₺"
    }

    val dailyLimit: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DAILY_LIMIT_KEY] ?: ""
    }

    val monthlyLimit: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[MONTHLY_LIMIT_KEY] ?: ""
    }

    suspend fun setDefaultCurrency(currency: String) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_CURRENCY_KEY] = currency
        }
    }

    suspend fun setDailyLimit(limit: String) {
        context.dataStore.edit { preferences ->
            preferences[DAILY_LIMIT_KEY] = limit
        }
    }

    suspend fun setMonthlyLimit(limit: String) {
        context.dataStore.edit { preferences ->
            preferences[MONTHLY_LIMIT_KEY] = limit
        }
    }
}