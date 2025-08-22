package com.waxd.pos.fcmb.model

data class StatusCounts(
    var totalCount: Int = 0,
    var pendingCount: Int = 0,
    var failedCount: Int = 0,
    var successCount: Int = 0,
    var inProgressCount: Int = 0
)