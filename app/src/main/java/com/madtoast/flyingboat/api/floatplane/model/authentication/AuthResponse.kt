package com.madtoast.flyingboat.api.floatplane.model.authentication

import com.madtoast.flyingboat.api.floatplane.model.user.User

data class AuthResponse(
    val user: User?,
    val needs2FA: Boolean
)