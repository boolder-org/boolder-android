package com.boolder.boolder.offline

import androidx.work.WorkInfo

// Progress keys
const val WORK_DATA_PROGRESS = "progress"
const val WORK_DATA_PROGRESS_DETAIL = "progress_detail"

val DOWNLOAD_TERMINATED_STATUSES = arrayOf(
    WorkInfo.State.SUCCEEDED,
    WorkInfo.State.FAILED,
    WorkInfo.State.CANCELLED
)
