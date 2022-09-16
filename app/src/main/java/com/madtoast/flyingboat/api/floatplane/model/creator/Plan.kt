package com.madtoast.flyingboat.api.floatplane.model.creator

import com.madtoast.flyingboat.api.floatplane.model.BaseApiResponse
import com.madtoast.flyingboat.api.floatplane.model.content.Image

class Plan : BaseApiResponse() {
    val allowGrandfatheredAccess: Boolean = false;
    val creator: String? = null;
    val currency: String? = null;
    val description: String? = null;
    val discordRoles: Array<DiscordRole>? = null;
    val discordServer: Array<DiscordServer>? = null;
    val enabled: Boolean = false;
    val enabledGlobal: Boolean = false;
    val featured: Boolean = false;
    val interval: String? = null;
    val logo: Image? = null;
    val price: String? = null;
    val priceYearly: String? = null;
    val title: String? = null;
    val trialPeriod: Int? = null;
    val updatedAt: String? = null;
    val userIsGrandfathered: Boolean = false;
    val userIsSubscribed: Boolean = false;
}