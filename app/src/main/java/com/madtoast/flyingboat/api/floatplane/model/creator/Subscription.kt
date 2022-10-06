package com.madtoast.flyingboat.api.floatplane.model.creator

data class Subscription(
    val startDate: String?,
    val endDate: String?,
    val paymentID: Int,
    val interval: String?,
    val paymentCancelled: Boolean,
    val plan: Plan?,
    val creator: String?
)