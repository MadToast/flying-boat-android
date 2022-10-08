package com.madtoast.flyingboat.ui.activities.ui.login

import com.madtoast.flyingboat.api.floatplane.model.authentication.AuthResponse

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginResult(
    val success: AuthResponse? = null,
    val error: Int? = null
)