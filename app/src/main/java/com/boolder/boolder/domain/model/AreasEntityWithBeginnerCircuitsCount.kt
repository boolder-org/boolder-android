package com.boolder.boolder.domain.model

import androidx.room.Embedded
import com.boolder.boolder.data.database.entity.AreaEntity

data class AreasEntityWithBeginnerCircuitsCount(
    @Embedded val areaEntity: AreaEntity,
    val beginnerCircuitsCount: Int
)
