package com.boolder.boolder.domain.model

import android.os.Parcelable
import com.mapbox.maps.CoordinateBounds
import kotlinx.parcelize.Parcelize

@Parcelize
data class Circuit(
    val id: Int,
    val color: CircuitColor,
    val averageGrade: String,
    val isBeginnerFriendly: Boolean,
    val isDangerous: Boolean,
    val coordinateBounds: CoordinateBounds
) : Parcelable
