package com.boolder.boolder.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val PREF_CUSTOM_GRADE_RANGE_MIN = stringPreferencesKey("pref_custom_grade_range_min")
val PREF_CUSTOM_GRADE_RANGE_MAX = stringPreferencesKey("pref_custom_grade_range_max")

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
