package com.madtoast.flyingboat.api.floatplane.model.creator

import com.madtoast.flyingboat.api.floatplane.model.content.Category
import com.madtoast.flyingboat.api.floatplane.model.content.Image
import com.madtoast.flyingboat.api.floatplane.model.content.LiveStream

data class Creator(
    val id: String?,
    val owner: Any?,
    val title: String?,
    val urlname: String?,
    val description: String?,
    val discoverable: Boolean,
    val about: String?,
    val category: Category?,
    val cover: Image?,
    val icon: Image?,
    val liveStream: LiveStream?,
    val subscriptionPlans: Array<Plan>?,
    val subscriberCountDisplay: String?,
    val incomeDisplay: Boolean,
    val socialLinks: Map<String, String>?,
    val createdAt: String?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Creator

        if (id != other.id) return false
        if (owner != other.owner) return false
        if (title != other.title) return false
        if (urlname != other.urlname) return false
        if (description != other.description) return false
        if (discoverable != other.discoverable) return false
        if (about != other.about) return false
        if (category != other.category) return false
        if (cover != other.cover) return false
        if (icon != other.icon) return false
        if (liveStream != other.liveStream) return false
        if (subscriptionPlans != null) {
            if (other.subscriptionPlans == null) return false
            if (!subscriptionPlans.contentEquals(other.subscriptionPlans)) return false
        } else if (other.subscriptionPlans != null) return false
        if (subscriberCountDisplay != other.subscriberCountDisplay) return false
        if (incomeDisplay != other.incomeDisplay) return false
        if (socialLinks != other.socialLinks) return false
        if (createdAt != other.createdAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (owner?.hashCode() ?: 0)
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (urlname?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + discoverable.hashCode()
        result = 31 * result + (about?.hashCode() ?: 0)
        result = 31 * result + (category?.hashCode() ?: 0)
        result = 31 * result + (cover?.hashCode() ?: 0)
        result = 31 * result + (icon?.hashCode() ?: 0)
        result = 31 * result + (liveStream?.hashCode() ?: 0)
        result = 31 * result + (subscriptionPlans?.contentHashCode() ?: 0)
        result = 31 * result + (subscriberCountDisplay?.hashCode() ?: 0)
        result = 31 * result + incomeDisplay.hashCode()
        result = 31 * result + (socialLinks?.hashCode() ?: 0)
        result = 31 * result + (createdAt?.hashCode() ?: 0)
        return result
    }
}