package id.aseta.app.data.source.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

object PowerPreference {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private val POWER_LEVEL_KEY = floatPreferencesKey("power_level")

    suspend fun savePowerLevel(context: Context, level: Float) {
        context.dataStore.edit { settings ->
            settings[POWER_LEVEL_KEY] = level
        }
    }

    fun getPowerLevel(context: Context): Flow<Float> {
        return context.dataStore.data
            .map { prefs -> prefs[POWER_LEVEL_KEY] ?: 30f }
    }

    suspend fun getPowerLevelInit(context: Context): Float {
        return context.dataStore.data
            .map { prefs -> prefs[POWER_LEVEL_KEY] ?: 30f }.first()
    }

}