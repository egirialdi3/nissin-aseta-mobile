package id.aseta.app.data.source.local

import GetMenuItem
import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import id.aseta.app.data.model.LocationItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.json.JSONObject

val Context.dataStore by preferencesDataStore(name = "aseta_prefs")

object TokenDataStore {
    private val TOKEN_KEY = stringPreferencesKey("token")
    private val DATA_USER_KEY = stringPreferencesKey("data_user")
    private val SAVE_HH_KEY = stringPreferencesKey("data_hh")
    private val CONNECT_HH_KEY = stringPreferencesKey("last_bluetooth")
    private val SELECTED_LOCATION_KEY = stringPreferencesKey("selected_location")


    private val gson = Gson()


    suspend fun saveSelectedLocation(context: Context, locationJson: JSONObject) {
        context.dataStore.edit { prefs ->
            prefs[SELECTED_LOCATION_KEY] = locationJson.toString()
        }
    }

    suspend fun saveSelectedHH(context: Context,type: String,macAddress: String) {
        context.dataStore.edit { prefs ->
            prefs[SAVE_HH_KEY] =type
            prefs[CONNECT_HH_KEY] = macAddress
        }

    }

    suspend fun getSelectedAddress(context: Context): String {
        val prefs = context.dataStore.data.first()
        return prefs[CONNECT_HH_KEY] ?: ""
    }


    suspend fun saveSelectedHHType1(context: Context) {
        context.dataStore.edit { prefs ->
            prefs[SAVE_HH_KEY] ="1"
        }

    }
    suspend fun getSelectedHH(context: Context): String {
        val jsonString = context.dataStore.data
            .map { it[SAVE_HH_KEY] ?: return@map null }
            .firstOrNull() ?: return ""

        return jsonString
    }



    suspend fun getSelectedLocation(context: Context): LocationItem? {
        val jsonString = context.dataStore.data
            .map { it[SELECTED_LOCATION_KEY] ?: return@map null }
            .firstOrNull() ?: return null

        val json = JSONObject(jsonString)
        println(json.getString("location"))

        return LocationItem(
            location_id = json.getString("location_id"),
            location = json.getString("location"),
            full_location = json.getString("full_location"),
            parent = null,
            group = null,
            level = 1,
            sort = 0,
            area = "",
            detail = null,
            process_area = 0
        )
    }

    suspend fun saveDataUser(context: Context, dataUser: GetMenuItem?) {
        context.dataStore.edit { prefs ->
            prefs[DATA_USER_KEY] = gson.toJson(dataUser)
        }
    }

    suspend fun getDataUser(context: Context): GetMenuItem? {
        val jsonString = context.dataStore.data
            .map { prefs -> prefs[DATA_USER_KEY] ?: return@map null }
            .first()

        return jsonString?.let {
            gson.fromJson(it, GetMenuItem::class.java)
        }
    }

    suspend fun saveToken(context: Context, token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    suspend fun getToken(context: Context): String? {
        return context.dataStore.data.map { prefs ->
            prefs[TOKEN_KEY]
        }.first()
    }

    suspend fun clearToken(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}