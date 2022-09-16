package com.madtoast.flyingboat.api.floatplane.model.user

import com.madtoast.flyingboat.api.floatplane.model.BaseApiResponse

class UserContainer : BaseApiResponse() {
    val user: User? = null;
    val needs2FA: Boolean = false;
}