package com.madtoast.flyingboat.api.floatplane.model.user

import com.madtoast.flyingboat.api.floatplane.model.BaseApiResponse
import com.madtoast.flyingboat.api.floatplane.model.content.Image
import com.madtoast.flyingboat.api.floatplane.model.creator.Creator

class User : BaseApiResponse() {
    val username: String? = null;
    val profileImage: Image? = null;
    val email: String? = null;
    val displayName: String? = null;
    val creators: Array<Creator>? = null;
}