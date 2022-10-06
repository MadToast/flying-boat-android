package com.madtoast.flyingboat.api.floatplane.model.cdn

import com.madtoast.flyingboat.api.floatplane.model.content.Resource

data class DeliveryInfo(
    val cdn: String?,
    val strategy: String?,
    val resource: Resource?
)