package com.madtoast.flyingboat.api.floatplane.model.comment

import com.madtoast.flyingboat.api.floatplane.model.enums.Interaction
import com.madtoast.flyingboat.api.floatplane.model.user.User

data class Comment(
    val blogPost: String?,
    val user: User?,
    val contentReference: String?,
    val contentReferenceType: String?,
    val text: String?,
    val postDate: String?,
    val editDate: String?,
    val likes: Int,
    val dislikes: Int,
    val score: Int,
    val interactionCounts: Map<String, Int>?,
    val totalReplies: Int,
    val replies: Array<Comment>?,
    val userInteraction: Interaction?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Comment

        if (blogPost != other.blogPost) return false
        if (user != other.user) return false
        if (contentReference != other.contentReference) return false
        if (contentReferenceType != other.contentReferenceType) return false
        if (text != other.text) return false
        if (postDate != other.postDate) return false
        if (editDate != other.editDate) return false
        if (likes != other.likes) return false
        if (dislikes != other.dislikes) return false
        if (score != other.score) return false
        if (interactionCounts != other.interactionCounts) return false
        if (totalReplies != other.totalReplies) return false
        if (replies != null) {
            if (other.replies == null) return false
            if (!replies.contentEquals(other.replies)) return false
        } else if (other.replies != null) return false
        if (userInteraction != other.userInteraction) return false

        return true
    }

    override fun hashCode(): Int {
        var result = blogPost?.hashCode() ?: 0
        result = 31 * result + (user?.hashCode() ?: 0)
        result = 31 * result + (contentReference?.hashCode() ?: 0)
        result = 31 * result + (contentReferenceType?.hashCode() ?: 0)
        result = 31 * result + (text?.hashCode() ?: 0)
        result = 31 * result + (postDate?.hashCode() ?: 0)
        result = 31 * result + (editDate?.hashCode() ?: 0)
        result = 31 * result + likes
        result = 31 * result + dislikes
        result = 31 * result + score
        result = 31 * result + (interactionCounts?.hashCode() ?: 0)
        result = 31 * result + totalReplies
        result = 31 * result + (replies?.contentHashCode() ?: 0)
        result = 31 * result + (userInteraction?.hashCode() ?: 0)
        return result
    }
}