package com.madtoast.flyingboat.api.floatplane.model.authentication

data class AuthenticationRequest(
    val username: String,
    val password: String,
    val captchaToken: String? = null
)