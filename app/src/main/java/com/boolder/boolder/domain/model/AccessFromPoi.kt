package com.boolder.boolder.domain.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.boolder.boolder.R

data class AccessFromPoi(
    val distanceInMinutes: Int,
    val transport: PoiTransport,
    val type: PoiType,
    val name: String,
    val googleUrl: String
)

enum class PoiType(
    @DrawableRes val iconRes: Int,
    val dbValue: String,
) {
    PARKING(
        iconRes = R.drawable.ic_local_parking,
        dbValue = "parking"
    ),
    TRAIN_STATION(
        iconRes = R.drawable.ic_train,
        dbValue = "train_station"
    );

    companion object {
        fun fromDbValue(dbValue: String) = try {
            entries.first { it.dbValue == dbValue }
        } catch (e: Exception) {
            PARKING
        }
    }
}

enum class PoiTransport(
    @DrawableRes val iconRes: Int,
    val dbValue: String
) {
    WALKING(
        iconRes = R.drawable.ic_directions_walk,
        dbValue = "walking"
    ),
    BIKE(
        iconRes = R.drawable.ic_directions_bike,
        dbValue = "bike"
    );

    companion object {
        fun fromDbValue(dbValue: String) = try {
            entries.first { it.dbValue == dbValue }
        } catch (e: Exception) {
            WALKING
        }
    }
}
