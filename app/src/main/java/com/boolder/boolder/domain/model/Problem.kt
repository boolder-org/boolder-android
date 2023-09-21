package com.boolder.boolder.domain.model

import android.content.Context
import android.os.Parcelable
import androidx.annotation.ColorInt
import com.boolder.boolder.view.search.BaseObject
import kotlinx.parcelize.Parcelize


@Parcelize
data class Problem(
    val id: Int,
    val name: String?,
    val nameEn: String?,
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
    val areaName: String?,
    var state: Int?
) : Parcelable, BaseObject {

    val circuitColorSafe
        get() = circuitColor?.let { CircuitColor.valueOf(it.uppercase()) }
            ?: CircuitColor.OFF_CIRCUIT

    @ColorInt
    fun getColor(context: Context): Int = circuitColorSafe.getColor(context)

}
