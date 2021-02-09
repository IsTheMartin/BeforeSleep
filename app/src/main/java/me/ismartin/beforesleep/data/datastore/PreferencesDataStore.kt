package me.ismartin.beforesleep.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.createDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Created by martin.morales on 2/8/21
 */
class PreferencesDataStore @Inject constructor(
    @ApplicationContext context: Context
) {

    private val dataStore: DataStore<Preferences> = context.createDataStore(
        name = "preferences"
    )

    private val DEACTIVATE_BLUETOOTH = booleanPreferencesKey("bluetooth")
    private val DEACTIVATE_WIFI = booleanPreferencesKey("wifi")

    val willDeactivateBluetooth: Flow<Boolean> = dataStore.data.map{ prefs ->
        prefs[DEACTIVATE_BLUETOOTH]?: false
    }

    val willDeactivateWiFi: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[DEACTIVATE_WIFI]?: false
    }

    suspend fun deactivateBluetooth(value: Boolean) = dataStore.edit { prefs ->
        prefs[DEACTIVATE_BLUETOOTH] = value
    }

    suspend fun deactivateWiFi(value: Boolean) = dataStore.edit { prefs ->
        prefs[DEACTIVATE_WIFI] = value
    }
}