package com.madtoast.flyingboat.api.floatplane.model.creator

import com.madtoast.flyingboat.api.floatplane.model.BaseApiResponse
import com.madtoast.flyingboat.api.floatplane.model.content.Category
import com.madtoast.flyingboat.api.floatplane.model.content.Image
import com.madtoast.flyingboat.api.floatplane.model.content.LiveStream

class Creator : BaseApiResponse() {
    val owner: String? = null;
    val title: String? = null;
    val urlname: String? = null;
    val description: String? = null;
    val discoverable: Boolean = false;
    val about: String? = null;
    val category: Category? = null;
    val cover: Image? = null;
    val icon: Image? = null;
    val liveStream: LiveStream? = null;
    val subscriptionPlans: Array<Plan>? = null;
    val subscriberCountDisplay: String? = null;
    val incomeDisplay: Boolean = false;
    val socialLinks: Map<String, String>? = null;
}