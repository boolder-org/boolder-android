package com.boolder.boolder.domain.model

import android.content.Context
import android.os.Parcelable
import com.boolder.boolder.view.search.BaseObject
import kotlinx.parcelize.Parcelize


@Parcelize
data class Problem(
    val id: Int,
    val name: String?,
    val grade: String?,
    val latitude: Float,
    val longitude: Float,
    val circuitId: Int?,
    val circuitNumber: String?,
    val circuitColor: String?,
    val steepness: String,
    val sitStart: Boolean,
    val areaId: Int,
    val bleauInfoId: String?,
    val featured: Boolean,
    val parentId: Int?,
    val areaName: String?
) : Parcelable, BaseObject {

    val circuitColorSafe
        get() = circuitColor?.let { CircuitColor.valueOf(it.uppercase()) }
            ?: CircuitColor.OFF_CIRCUIT

    fun drawColor(context: Context): Int = circuitColorSafe.getColor(context)

}
