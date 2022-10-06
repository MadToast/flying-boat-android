package com.madtoast.flyingboat.api.floatplane.interfaces

import com.madtoast.flyingboat.api.floatplane.model.user.Users
import retrofit2.http.GET
import retrofit2.http.Query

interface UserV2 {
    @GET(URI_BASE + URI_INFO)
    suspend fun info(@Query("id") username: Array<String>): Users?

    @GET(URI_BASE + URI_NAMED)
    suspend fun named(@Query("username") username: Array<String>): Users?

    @GET(URI_BASE + URI_USER_BAN_STATUS)
    suspend fun banStatus(@Query("creator") creatorGUID: String): Boolean?

    companion object {
        private const val URI_BASE = "/api/v3/user"
        private const val URI_INFO = "/info"
        private const val URI_NAMED = "/named"
        private const val URI_USER_BAN_STATUS = "/ban/status"
    }
}