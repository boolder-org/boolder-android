package com.boolder.boolder.utils.previewgenerator

import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItem
import com.boolder.boolder.view.offlinephotos.model.OfflineAreaItemStatus

fun dummyOfflineAreaItem(
    status: OfflineAreaItemStatus = OfflineAreaItemStatus.NotDownloaded
) = OfflineAreaItem(
    area = dummyArea(),
    status = status
)
