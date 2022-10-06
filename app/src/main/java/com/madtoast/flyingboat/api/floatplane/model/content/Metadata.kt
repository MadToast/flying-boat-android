package com.madtoast.flyingboat.api.floatplane.model.content

data class Metadata(
    val hasVideo: Boolean,
    val videoCount: Int,
    val videoDuration: Int,
    val hasAudio: Boolean,
    val audioCount: Int,
    val audioDuration: Int,
    val hasPicture: Boolean,
    val pictureCount: Int,
    val hasGallery: Boolean,
    val galleryCount: Int,
    val isFeature: Boolean,
)