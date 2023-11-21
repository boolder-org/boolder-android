package com.boolder.boolder.domain.model

import androidx.room.Embedded
import com.boolder.boolder.data.database.entity.AreasEntity

data class AreasEntityWithBeginnerCircuitsCount(
    @Embedded val areaEntity: AreasEntity,
    val beginnerCircuitsCount: Int
)
