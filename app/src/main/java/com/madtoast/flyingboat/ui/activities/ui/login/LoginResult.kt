package com.madtoast.flyingboat.ui.activities.ui.login

/**
 * Authentication result : success (user details) or error message.
 */
data class UiResult<T : Any>(
    val success: T? = null,
    val error: Int? = null
)