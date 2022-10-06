package com.madtoast.flyingboat.api.floatplane.model.creator

import com.madtoast.flyingboat.api.floatplane.model.content.Image

data class Plan(
    val id: String?,
    val allowGrandfatheredAccess: Boolean,
    val creator: String?,
    val createdAt: String?,
    val currency: String?,
    val description: String?,
    val discordRoles: Array<DiscordRole>?,
    val discordServer: Array<DiscordServer>?,
    val enabled: Boolean,
    val enabledGlobal: Boolean,
    val featured: Boolean,
    val interval: String?,
    val logo: Image?,
    val price: String?,
    val priceYearly: String?,
    val title: String?,
    val trialPeriod: Int?,
    val updatedAt: String?,
    val userIsGrandfathered: Boolean,
    val userIsSubscribed: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Plan

        if (id != other.id) return false
        if (allowGrandfatheredAccess != other.allowGrandfatheredAccess) return false
        if (creator != other.creator) return false
        if (createdAt != other.createdAt) return false
        if (currency != other.currency) return false
        if (description != other.description) return false
        if (discordRoles != null) {
            if (other.discordRoles == null) return false
            if (!discordRoles.contentEquals(other.discordRoles)) return false
        } else if (other.discordRoles != null) return false
        if (discordServer != null) {
            if (other.discordServer == null) return false
            if (!discordServer.contentEquals(other.discordServer)) return false
        } else if (other.discordServer != null) return false
        if (enabled != other.enabled) return false
        if (enabledGlobal != other.enabledGlobal) return false
        if (featured != other.featured) return false
        if (interval != other.interval) return false
        if (logo != other.logo) return false
        if (price != other.price) return false
        if (priceYearly != other.priceYearly) return false
        if (title != other.title) return false
        if (trialPeriod != other.trialPeriod) return false
        if (updatedAt != other.updatedAt) return false
        if (userIsGrandfathered != other.userIsGrandfathered) return false
        if (userIsSubscribed != other.userIsSubscribed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + allowGrandfatheredAccess.hashCode()
        result = 31 * result + (creator?.hashCode() ?: 0)
        result = 31 * result + (createdAt?.hashCode() ?: 0)
        result = 31 * result + (currency?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (discordRoles?.contentHashCode() ?: 0)
        result = 31 * result + (discordServer?.contentHashCode() ?: 0)
        result = 31 * result + enabled.hashCode()
        result = 31 * result + enabledGlobal.hashCode()
        result = 31 * result + featured.hashCode()
        result = 31 * result + (interval?.hashCode() ?: 0)
        result = 31 * result + (logo?.hashCode() ?: 0)
        result = 31 * result + (price?.hashCode() ?: 0)
        result = 31 * result + (priceYearly?.hashCode() ?: 0)
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (trialPeriod ?: 0)
        result = 31 * result + (updatedAt?.hashCode() ?: 0)
        result = 31 * result + userIsGrandfathered.hashCode()
        result = 31 * result + userIsSubscribed.hashCode()
        return result
    }
}