package com.madtoast.flyingboat.api.floatplane.model.content

import com.madtoast.flyingboat.api.floatplane.model.enums.PostType

data class SubPost(
    val id: String?,
    val guid: String?,
    val title: String?,
    val type: PostType?,
    val description: String?,
    val releaseDate: String?,
    val duration: Int,
    val creator: String?,
    val likes: Int,
    val dislikes: Int,
    val score: Int,
    val isProcessing: Boolean,
    val primaryBlogPost: String?,
    val thumbnail: Image?,
    val isAccessible: Boolean,
    val waveform: Waveform?,
    val imageFiles: Array<Image>?,
    val createdAt: String?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SubPost

        if (id != other.id) return false
        if (guid != other.guid) return false
        if (title != other.title) return false
        if (type != other.type) return false
        if (description != other.description) return false
        if (releaseDate != other.releaseDate) return false
        if (duration != other.duration) return false
        if (creator != other.creator) return false
        if (likes != other.likes) return false
        if (dislikes != other.dislikes) return false
        if (score != other.score) return false
        if (isProcessing != other.isProcessing) return false
        if (primaryBlogPost != other.primaryBlogPost) return false
        if (thumbnail != other.thumbnail) return false
        if (isAccessible != other.isAccessible) return false
        if (waveform != other.waveform) return false
        if (imageFiles != null) {
            if (other.imageFiles == null) return false
            if (!imageFiles.contentEquals(other.imageFiles)) return false
        } else if (other.imageFiles != null) return false
        if (createdAt != other.createdAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (guid?.hashCode() ?: 0)
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (releaseDate?.hashCode() ?: 0)
        result = 31 * result + duration
        result = 31 * result + (creator?.hashCode() ?: 0)
        result = 31 * result + likes
        result = 31 * result + dislikes
        result = 31 * result + score
        result = 31 * result + isProcessing.hashCode()
        result = 31 * result + (primaryBlogPost?.hashCode() ?: 0)
        result = 31 * result + (thumbnail?.hashCode() ?: 0)
        result = 31 * result + isAccessible.hashCode()
        result = 31 * result + (waveform?.hashCode() ?: 0)
        result = 31 * result + (imageFiles?.contentHashCode() ?: 0)
        result = 31 * result + (createdAt?.hashCode() ?: 0)
        return result
    }
}