package com.madtoast.flyingboat.api.floatplane.model.content

data class LiveStream(
    val id: String?,
    val title: String?,
    val description: String?,
    val thumbnail: Image?,
    val owner: String?,
    val streamPath: String?,
    val offline: LiveStream?
)