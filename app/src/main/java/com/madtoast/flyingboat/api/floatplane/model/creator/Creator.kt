package com.madtoast.flyingboat.api.floatplane.model.creator

import com.madtoast.flyingboat.api.floatplane.model.content.Category
import com.madtoast.flyingboat.api.floatplane.model.content.Image
import com.madtoast.flyingboat.api.floatplane.model.content.LiveStream

data class Creator(
    val id: String? = null,
    val owner: Any? = null,
    val title: String? = null,
    val urlname: String? = null,
    val description: String? = null,
    val discoverable: Boolean = false,
    val about: String? = null,
    val category: Category? = null,
    val cover: Image? = null,
    val icon: Image? = null,
    val liveStream: LiveStream? = null,
    val subscriptionPlans: Array<Plan>? = null,
    val subscriberCountDisplay: String? = null,
    val incomeDisplay: Boolean = false,
    val socialLinks: Map<String, String>? = null,
    val createdAt: String? = null,
    var userSubscribed: Boolean = false
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
        return id?.hashCode() ?: 0
    }

    class SubscribedComparator : Comparator<Creator> {
        override fun compare(o1: Creator?, o2: Creator?): Int {
            return when {
                o1 == null && o2 == null -> 0
                o1 == null -> -1
                o2 == null -> 1
                else -> o1.userSubscribed.compareTo(o2.userSubscribed)
            }
        }
    }

    class CategoryComparator : Comparator<Creator> {
        override fun compare(o1: Creator?, o2: Creator?): Int {
            return when {
                o1?.category?.title == null && o2?.category?.title == null -> 0
                o1?.category?.title == null -> -1
                o2?.category?.title == null -> 1
                else -> o1.category.title.compareTo(o2.category.title)
            }
        }
    }
}