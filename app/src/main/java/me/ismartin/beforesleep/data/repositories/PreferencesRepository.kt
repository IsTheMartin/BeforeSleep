package me.ismartin.beforesleep.data.repositories

import kotlinx.coroutines.flow.first
import me.ismartin.beforesleep.data.datastore.PreferencesDataStore
import javax.inject.Inject

/**
 * Created by martin.morales on 2/8/21
 */
class PreferencesRepository @Inject constructor(
    private val preferences: PreferencesDataStore
) {

    suspend fun willDeactivateBluetooth(): Boolean {
        return preferences.willDeactivateBluetooth.first()
    }

    suspend fun willDeactivateWiFi(): Boolean {
        return preferences.willDeactivateWiFi.first()
    }

    suspend fun setDeactivateBluetooth(value: Boolean) = preferences.deactivateBluetooth(value)
    suspend fun setDeactivateWiFi(value: Boolean) = preferences.deactivateWiFi(value)

}