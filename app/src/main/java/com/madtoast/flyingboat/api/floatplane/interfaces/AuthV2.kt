package com.madtoast.flyingboat.api.floatplane.interfaces

import com.madtoast.flyingboat.api.floatplane.model.authentication.AuthResponse
import com.madtoast.flyingboat.api.floatplane.model.authentication.AuthTwoFactorRequest
import com.madtoast.flyingboat.api.floatplane.model.authentication.AuthenticationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthV2 {

    @POST(URI_BASE + URI_LOGIN)
    suspend fun login(@Body request: AuthenticationRequest): Response<AuthResponse?>

    @POST(URI_BASE + URI_LOGOUT)
    suspend fun logout(): String?

    @POST(URI_BASE + URI_CHECK2FA)
    suspend fun confirm2fa(@Body request: AuthTwoFactorRequest): Response<AuthResponse?>

    companion object {
        private const val URI_BASE = "/api/v2/auth"
        private const val URI_LOGIN = "/login"
        private const val URI_LOGOUT = "/logout"
        private const val URI_CHECK2FA = "/checkFor2faLogin"
    }
}