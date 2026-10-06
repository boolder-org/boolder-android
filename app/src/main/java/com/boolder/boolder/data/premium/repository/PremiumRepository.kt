package com.boolder.boolder.data.premium.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.boolder.boolder.data.datastore.PREF_PREMIUM_SUBSCRIBED
import com.boolder.boolder.data.premium.model.PremiumStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class PremiumRepository(
    private val dataStore: DataStore<Preferences>
) {

    private val _status = MutableStateFlow(readCachedStatus())
    val status: StateFlow<PremiumStatus> = _status.asStateFlow()

    private fun readCachedStatus(): PremiumStatus {
        val isSubscribed = runBlocking { dataStore.data.first()[PREF_PREMIUM_SUBSCRIBED] } ?: false

        return if (isSubscribed) PremiumStatus.SUBSCRIBED else PremiumStatus.NOT_SUBSCRIBED
    }
}
