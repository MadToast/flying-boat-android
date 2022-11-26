package com.madtoast.flyingboat.api.floatplane.model.content

import com.madtoast.flyingboat.api.floatplane.model.creator.Creator
import com.madtoast.flyingboat.api.floatplane.model.enums.Interaction
import com.madtoast.flyingboat.api.floatplane.model.enums.PostType

data class Post(
    val id: String?,
    val guid: String?,
    val title: String?,
    val text: String?,
    val type: PostType?,
    val tags: Array<String>?,
    val attachmentOrder: Array<String>?,
    val metadata: Metadata?,
    val releaseDate: String?,
    val likes: Int,
    val dislikes: Int,
    val score: Int,
    val comments: Int,
    val creator: Creator?,
    val wasReleasedSilently: Boolean,
    val thumbnail: Image?,
    val isAccessible: Boolean,
    val userInteraction: Interaction?,
    val videoAttachments: Array<Any>?,
    val audioAttachments: Array<Any>?,
    val createdAt: String?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Post

        if (id != other.id) return false
        if (guid != other.guid) return false
        if (title != other.title) return false
        if (text != other.text) return false
        if (type != other.type) return false
        if (tags != null) {
            if (other.tags == null) return false
            if (!tags.contentEquals(other.tags)) return false
        } else if (other.tags != null) return false
        if (attachmentOrder != null) {
            if (other.attachmentOrder == null) return false
            if (!attachmentOrder.contentEquals(other.attachmentOrder)) return false
        } else if (other.attachmentOrder != null) return false
        if (metadata != other.metadata) return false
        if (releaseDate != other.releaseDate) return false
        if (likes != other.likes) return false
        if (dislikes != other.dislikes) return false
        if (score != other.score) return false
        if (comments != other.comments) return false
        if (creator != other.creator) return false
        if (wasReleasedSilently != other.wasReleasedSilently) return false
        if (thumbnail != other.thumbnail) return false
        if (isAccessible != other.isAccessible) return false
        if (userInteraction != other.userInteraction) return false
        if (videoAttachments != null) {
            if (other.videoAttachments == null) return false
            if (!videoAttachments.contentEquals(other.videoAttachments)) return false
        } else if (other.videoAttachments != null) return false
        if (audioAttachments != null) {
            if (other.audioAttachments == null) return false
            if (!audioAttachments.contentEquals(other.audioAttachments)) return false
        } else if (other.audioAttachments != null) return false
        if (createdAt != other.createdAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (guid?.hashCode() ?: 0)
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (text?.hashCode() ?: 0)
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + (tags?.contentHashCode() ?: 0)
        result = 31 * result + (attachmentOrder?.contentHashCode() ?: 0)
        result = 31 * result + (metadata?.hashCode() ?: 0)
        result = 31 * result + (releaseDate?.hashCode() ?: 0)
        result = 31 * result + likes
        result = 31 * result + dislikes
        result = 31 * result + score
        result = 31 * result + comments
        result = 31 * result + (creator?.hashCode() ?: 0)
        result = 31 * result + wasReleasedSilently.hashCode()
        result = 31 * result + (thumbnail?.hashCode() ?: 0)
        result = 31 * result + isAccessible.hashCode()
        result = 31 * result + (userInteraction?.hashCode() ?: 0)
        result = 31 * result + (videoAttachments?.contentHashCode() ?: 0)
        result = 31 * result + (audioAttachments?.contentHashCode() ?: 0)
        result = 31 * result + (createdAt?.hashCode() ?: 0)
        return result
    }
}