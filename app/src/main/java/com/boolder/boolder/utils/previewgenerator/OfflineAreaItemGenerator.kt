package com.boolder.boolder.utils.previewgenerator

import com.boolder.boolder.domain.model.Area
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItem
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItemStatus

fun dummyOfflineAreaItem(
    area: Area = dummyArea(),
    status: OfflineAreaItemStatus = OfflineAreaItemStatus.NotDownloaded
) = OfflineAreaItem(
    area = area,
    status = status
)
